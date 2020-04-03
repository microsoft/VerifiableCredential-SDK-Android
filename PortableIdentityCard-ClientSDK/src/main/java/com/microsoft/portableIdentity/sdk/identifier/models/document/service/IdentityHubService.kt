package com.microsoft.portableIdentity.sdk.identifier.models.document.service

import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IdentityHub") // serializer writes this value for "type"
class IdentityHubService(override val id: String, override val serviceEndpoint: String) :
    IdentifierDocumentService {
    override val type: String = "IdentityHub"

    companion object {
        fun create(id: String, signatureKeyRef: String, instances: List<Identifier>, keyStore: KeyStore): IdentifierDocumentService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw SdkLog.error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdentityHubService(
                id, "https://testendpoint"
            )
        }
    }
}