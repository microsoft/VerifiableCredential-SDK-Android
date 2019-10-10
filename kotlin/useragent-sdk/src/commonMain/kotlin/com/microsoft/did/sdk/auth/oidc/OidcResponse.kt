package com.microsoft.did.sdk

import com.microsoft.did.sdk.auth.OAuthRequestParameter
import com.microsoft.did.sdk.auth.oidc.OidcRequest
import com.microsoft.did.sdk.credentials.ClaimDetail
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.getCurrentTime
import com.microsoft.did.sdk.utilities.getHttpClient
import com.microsoft.did.sdk.utilities.stringToByteArray
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.floor

class OidcResponse (
    val responder: Identifier,
    val nonce: String,
    val state: String? = null,
    val claims: MutableList<ClaimObject> = mutableListOf(),
    private val scope: String,
    private val redirectUrl: String,
    private val responseType: String,
    private val responseMode: String
    ) {
    @Serializable
    data class OidcResponseObject(
        @Required
        val iss: String = SELFISSUED,
        val sub: String, // thumbprint (sha-256)
        val aud: String,
        val nonce: String,
        @SerialName("did_comm")
        val didComm: DidComm ? = null,
        @SerialName("sub_jwk")
        val subJwk: JsonWebKey,
        val iat: Int,
        val exp: Int,
        @SerialName("_claim_names")
        val claimNames: Map<String, String>? = null,
        @SerialName("_claim_sources")
        val claimSources: Map<String, Map<String, String>>? = null
    )

    @Serializable
    data class DidComm(
        val did: String
    )

    companion object {
        const val SELFISSUED = "https://self-issued.me"

        fun create(oidcRequest: OidcRequest, respondWithIdentifier: Identifier): OidcResponse {
            return OidcResponse(
                responder = respondWithIdentifier,
                nonce = oidcRequest.nonce,
                state = oidcRequest.state,
                scope = oidcRequest.scope,
                redirectUrl = oidcRequest.redirectUrl,
                responseType = oidcRequest.responseType,
                responseMode = oidcRequest.responseMode
            )
        }

        fun parseAndVerify(token: String,
                           crypto: CryptoOperations,
                           resolver: IResolver): OidcResponse {
            TODO("Not yet implemented")
        }
    }

    fun addClaim(claim: ClaimObject) {
        this.claims.add(claim)
    }

    /**
     * @param expiresIn Minutes until the OIDC response requires
     */
    @ImplicitReflectionSerializer
    suspend fun signAndSend(
        crypto: CryptoOperations,
        expiresIn: Int = 5,
        useKey: String = responder.signatureKeyReference
    ): ClaimObject? {
        val currentTime = getCurrentTime()
        val expiration = currentTime + 1000 * 60 * expiresIn
        val exp = floor(expiration / 1000f).toInt()
        val iat = floor( currentTime / 1000f).toInt()
        val key = crypto.keyStore.getPublicKey(useKey).getKey()

        @Serializable
        data class PresentationDetails(
            val iss: String,
            val aud: String,
            val vc: List<ClaimDetail>
        )

        var claimNames: MutableMap<String, String>? = null
        var claimSources: MutableMap<String, Map<String, String>>? = null
        if (!claims.isNullOrEmpty()) {
            claimNames = mutableMapOf()
            claimSources = mutableMapOf()
            claims.forEachIndexed { index, it ->
                val presentation = PresentationDetails(
                    it.claimIssuer,
                    redirectUrl,
                    it.claimDetails
                )
                val presentationData = MinimalJson.serializer.stringify(PresentationDetails.serializer(), presentation)
                val token = JwsToken(presentationData)
                token.sign(useKey, crypto)
                val serialized = token.serialize(JwsFormat.Compact)
                val name = "src$index"
                claimNames[it.claimClass] = name
                claimSources[name] = mapOf(
                    "VP_JWT" to serialized
                )
            }
        }

        val response = OidcResponseObject(
            sub = key.getThumbprint(crypto, Sha.Sha256),
            aud = redirectUrl,
            nonce = nonce,
            didComm = DidComm(responder.document.id),
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            claimNames = claimNames,
            claimSources = claimSources
        )

        val responseData = MinimalJson.serializer.stringify(OidcResponseObject.serializer(), response)
        val token = JwsToken(responseData)
        token.sign(useKey, crypto)
        val responseSerialized = token.serialize(JwsFormat.Compact)

        return send(responseSerialized)
    }

    suspend fun send(idToken: String): ClaimObject? {
        return when (responseMode) {
            OidcRequest.defaultResponseMode -> {
                val responseBody = "id_token=$idToken" + if (!state.isNullOrBlank()) {
                    "&state=$state"
                } else {
                    ""
                }
                val response = getHttpClient().post<String> {
                    url(redirectUrl)
                    body = ByteArrayContent(
                        bytes = stringToByteArray(responseBody),
                        contentType = ContentType.Application.FormUrlEncoded)
                }
                if (response.isNotBlank()) {
                    MinimalJson.serializer.parse(ClaimObject.serializer(), response)
                } else {
                    null
                }
            }
            else -> {
                throw Error("Unknown Response Mode $responseMode")
            }
        }
    }
}