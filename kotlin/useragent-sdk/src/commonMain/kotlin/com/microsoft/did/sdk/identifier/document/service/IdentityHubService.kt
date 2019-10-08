package com.microsoft.did.sdk.identifier.document.service

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierDocumentService
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IdentityHub") // serializer writes this value for "type"
class IdentityHubService(override val id: String, val publicKey: String, @Polymorphic override val serviceEndpoint: Endpoint): IdentifierDocumentService {
    override val type: String = "IdentityHub"
    companion object {
        fun create(id: String, signatureKeyRef: String, instances: List<Identifier>, keyStore: IKeyStore): IdentifierDocumentService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw Error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdentityHubService(id, keyId.getLatestKeyId(), UserHubEndpoint(didList))
        }
    }
}