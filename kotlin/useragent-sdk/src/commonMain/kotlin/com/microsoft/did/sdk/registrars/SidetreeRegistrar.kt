package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.getHttpClient
import com.microsoft.did.sdk.utilities.stringToByteArray
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

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
        jwsToken.sign(signatureKeyRef, crypto, mapOf("kid" to "#$kid", "operation" to "create"))
        val jws = jwsToken.serialize(JwsFormat.FlatJson)
        println(jws)
        val response = sendRequest(jws)
        return MinimalJson.serializer.parse(IdentifierDocument.serializer(), response)
    }

    /**
     * Send request to the registration service
     * returning the fully discoverable Identifier Document.
     * @param request request sent to the registration service.
     */
    private suspend fun sendRequest(request: String): String {
        val client = getHttpClient()
        val response = client.post<String> {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            body = ByteArrayContent(
                bytes = stringToByteArray(request)
            )
        }
        client.close()
        return response
    }
}