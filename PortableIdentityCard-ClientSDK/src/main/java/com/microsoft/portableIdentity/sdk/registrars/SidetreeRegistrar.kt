// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.*
import io.ktor.client.features.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Named

/**
 * Registrar implementation for the Sidetree network
 * @class
 * @implements IRegistrar
 * @param registrarUrl to the registration endpoint
 * @param cryptoOperations
 */
class SidetreeRegistrar @Inject constructor(@Named("registrationUrl") private val baseUrl: String, private val serializer: Serializer): IRegistrar() {
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        // create JWS request
        val content = serializer.stringify(RegistrationDocument.serializer(), document)
        val jwsToken = JwsToken(content, serializer)
        val key = crypto.keyStore.getPublicKey(signatureKeyRef).getKey() as EllipticCurvePublicKey
        val kid = key.kid
        jwsToken.sign(signatureKeyRef, crypto, mapOf("kid" to kid, "operation" to "create", "alg" to "ES256K"))
        val jws = jwsToken.serialize(serializer, JwsFormat.FlatJson)
        jwsToken.verify(crypto);
        val response = sendRequest(jws)
        println(response)
        return serializer.parse(IdentifierDocument.serializer(), response)
    }

    /**
     * Send request to the registration service
     * returning the fully discoverable Identifier Document.
     * @param request request sent to the registration service.
     */
    private suspend fun sendRequest(request: String): String {
        val client = getHttpClient()
        try {
            return client.post<String> {
                url(baseUrl)
                body = ByteArrayContent(
                    bytes = stringToByteArray(request),
                    contentType = ContentType.Application.Json
                )
            }
        } catch (error: ResponseException) {
            println("Registration failed (${error.response.status})")
            while (!error.response.content.isClosedForRead) {
                val data = error.response.content.readUTF8Line(error.response.content.availableForRead)
                if (!data.isNullOrBlank()) {
                    println(data)
                }
            }
            throw error
        } finally {
            client.close()
        }
    }
}