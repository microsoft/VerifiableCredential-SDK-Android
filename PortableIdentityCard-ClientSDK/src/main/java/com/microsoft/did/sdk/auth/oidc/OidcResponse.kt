// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.auth.oidc

import com.microsoft.did.sdk.auth.OAuthRequestParameter
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.credentials.ClaimResponse
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.*
import com.microsoft.did.sdk.utilities.Serializer
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import kotlinx.serialization.*
import java.util.*
import kotlin.math.floor

class OidcResponse (
    val responder: Identifier,
    val crypto: CryptoOperations,
    private val logger: ILogger,
    val nonce: String,
    val state: String? = null,
    val claims: MutableList<ClaimObject> = mutableListOf(),
    private val redirectUrl: String,
    private val responseMode: String
    ) {
    @Serializable
    data class OidcResponseObject(
        @Required
        val iss: String = SELFISSUED,
        val sub: String, // thumbprint (sha-256)
        val aud: String,
        val nonce: String,
        val did: String?,
        @SerialName("sub_jwk")
        val subJwk: JsonWebKey,
        val iat: Int,
        val exp: Int,

        // NON COMPLIANT STATE
        val state: String? = null,
        @SerialName("_claim_names")
        val claimNames: Map<String, String>? = null,
        @SerialName("_claim_sources")
        val claimSources: Map<String, List<Map<String, String>>>? = null
    )

    companion object {
        const val SELFISSUED = "https://self-issued.me"

        fun create(oidcRequest: OidcRequest, respondWithIdentifier: Identifier, logger: ILogger): OidcResponse {
            return OidcResponse(
                responder = respondWithIdentifier,
                crypto = oidcRequest.crypto,
                logger = logger,
                nonce = oidcRequest.nonce,
                state = oidcRequest.state,
                redirectUrl = oidcRequest.redirectUrl,
                responseMode = oidcRequest.responseMode
            )
        }

        suspend fun parseAndVerify(data: String,
                                   clockSkewInMinutes: Int = 5,
                                   issuedWithinLastMinutes: Int? = null,
                                   crypto: CryptoOperations,
                                   logger: ILogger,
                                   resolver: IResolver,
                                   contentType: ContentType): OidcResponse {
            return when(contentType) {
                ContentType.Application.FormUrlEncoded -> {
                    val idToken = getQueryStringParameter(OAuthRequestParameter.IdToken, data, logger = logger) ?: throw logger.error("No id_token given.")
                    val state = getQueryStringParameter(OAuthRequestParameter.State, data, logger = logger)
                    val token = JwsToken(idToken, logger = logger)
                    val response = Serializer.parse(OidcResponseObject.serializer(), token.content())

                    val clockSkew = clockSkewInMinutes * 60
                    val currentTime = Date().time / 1000
                    if (currentTime - clockSkew < response.exp) {
                        throw logger.error("Id token has expired.")
                    }
                    if (issuedWithinLastMinutes != null &&
                        (response.iat < (currentTime - clockSkew - (issuedWithinLastMinutes * 60)))) {
                        throw logger.error("Id token issued before time frame set by issuedWithinLastMinutes ($issuedWithinLastMinutes)")
                    }

                    val responder = if (response.did != null) {
                        resolver.resolve(response.did, crypto)
                    } else {
                        DidKeyResolver.resolveIdentiferFromKid(token.signatures.first {
                            !it.getKid(logger = logger).isNullOrBlank()
                        }.getKid(logger = logger)!!, crypto, resolver, logger = logger)
                    }

                    DidKeyResolver.verifyJws(token, crypto, responder, logger = logger)
                    val claimObjects = mutableListOf<ClaimObject>()
                    if (response.claimNames != null) {
                        // for each claim class
                        response.claimNames.forEach {
                            claimClass ->
                            val claims = response.claimSources?.get(claimClass.value) ?: throw logger.error("Could not find claims for ${claimClass.key}")
                            claims.forEach { claim ->
                                if (claim.containsKey("JWT")) {
                                    val claimObjectData = JwsToken(claim["JWT"]!!, logger = logger)
                                    DidKeyResolver.verifyJws(claimObjectData, crypto, responder, logger = logger)
                                    val claimObject = Serializer.parse(ClaimObject.serializer(), claimObjectData.content())
                                    if (claimObject.claimClass != claimClass.key) {
                                        throw logger.error("Claim Object class does not match expected class.")
                                    }
                                    claimObject.verify(crypto, resolver, logger = logger)
                                    claimObjects.add(claimObject)
                                }
                            }
                        }
                    }

                    OidcResponse(
                        responder,
                        crypto,
                        logger,
                        response.nonce,
                        state,
                        claimObjects,
                        response.aud,
                        "form_post"
                    )
                }
                else -> {
                    throw logger.error("Unable to parse content of type $contentType")
                }
            }
        }
    }

    fun addClaim(claim: ClaimObject) {
        this.claims.add(claim)
    }

    /**
     * @param expiresIn Minutes until the OIDC response requires
     */
    suspend fun signAndSend(
        expiresIn: Int = 5,
        useKey: String = responder.signatureKeyReference
    ): ClaimObject? {
        val currentTime = Date().time
        val expiration = currentTime + 1000 * 60 * expiresIn
        val exp = floor(expiration / 1000f).toInt()
        val iat = floor( currentTime / 1000f).toInt()
        val key = crypto.keyStore.getPublicKey(useKey).getKey()

        var claimNames: MutableMap<String, String>? = null
        var claimSources: MutableMap<String, MutableList<Map<String, String>>>? = null
        if (!claims.isNullOrEmpty()) {
            claimNames = mutableMapOf()
            claimSources = mutableMapOf()
            claims.forEachIndexed { index, it ->
                val claimData = Serializer.stringify(ClaimObject.serializer(), it)
                val token = JwsToken(claimData, logger = logger)
                token.sign(useKey, crypto)
                val serialized = token.serialize(JwsFormat.Compact)
                val name = if (claimNames.containsKey(it.claimClass)) {
                    claimNames[it.claimClass]!!
                } else {
                    claimNames[it.claimClass] = "src$index"
                    "src$index"
                }
                if (!claimSources.containsKey(name)) {
                    claimSources[name] = mutableListOf()
                }
                claimSources[name]?.add(mapOf(
                    "JWT" to serialized
                ))
            }
        }

        val response = OidcResponseObject(
            sub = key.getThumbprint(crypto, Sha.Sha256),
            aud = redirectUrl,
            nonce = nonce,
            did = responder.document.id,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = state,
            claimNames = claimNames,
            claimSources = claimSources
        )
        val responseData = Serializer.stringify(OidcResponseObject.serializer(), response)
        println("Responding with data: $responseData")
        val token = JwsToken(responseData, logger = logger)
        token.sign(useKey, crypto)
        val responseSerialized = token.serialize(JwsFormat.Compact)

        return send(responseSerialized)?.claimObject
    }

    private suspend fun send(idToken: String): ClaimResponse? {
        return when (responseMode) {
            OidcRequest.defaultResponseMode -> {
//                val responseBody = "id_token=${idToken}" + if (!state.isNullOrBlank()) {
//                    "&state=${state}"
                    // DISABLED WHILE EnterpiseAgent is not percent decoding
                val responseBody = "id_token=${PercentEncoding.encode(idToken, logger = logger)}" + if (!state.isNullOrBlank()) {
                    "&state=${PercentEncoding.encode(state, logger = logger)}"
                } else {
                    ""
                }
                println("Encoded as: $responseBody")
                val response = getHttpClient().post<String> {
                    url(redirectUrl)
                    body = ByteArrayContent(
                        bytes = stringToByteArray(responseBody),
                        contentType = ContentType.Application.FormUrlEncoded)
                }
                if (response.isNotBlank()) {
                    try {
                        Serializer.parse(ClaimResponse.serializer(), response)
                    } catch (error: SerializationException) {
                        // this was not the right format but we did not get a 400 error
                        null
                    }
                } else {
                    null
                }
            }
            else -> {
                throw logger.error("Unknown Response Mode $responseMode")
            }
        }
    }
}