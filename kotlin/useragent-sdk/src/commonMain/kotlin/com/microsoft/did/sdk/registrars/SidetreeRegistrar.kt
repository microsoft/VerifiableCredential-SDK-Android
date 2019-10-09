package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.getHttpClient
import com.microsoft.did.sdk.utilities.stringToByteArray
import io.ktor.client.features.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import kotlinx.serialization.ImplicitReflectionSerializer
/**
 * Registrar implementation for the Sidetree network
 * @class
 * @implements IRegistrar
 * @param registrarUrl to the registration endpoint
 * @param cryptoOperations
 */
class SidetreeRegistrar(private val baseUrl: String): IRegistrar() {
    @ImplicitReflectionSerializer
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        // create JWS request
        val content = MinimalJson.serializer.stringify(RegistrationDocument.serializer(), document)
        val jwsToken = JwsToken(content)
        val kid = crypto.keyStore.getPublicKey(signatureKeyRef).getKey().kid
        jwsToken.sign(signatureKeyRef, crypto, mapOf("kid" to "#$kid", "operation" to "create", "alg" to "ES256K"))
        val jws = jwsToken.serialize(JwsFormat.FlatJson)
        println(jws)
        val response = sendRequest(jws)
        println(response)
        return MinimalJson.serializer.parse(IdentifierDocument.serializer(), response)
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