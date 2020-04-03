package com.microsoft.portableIdentity.sdk.identifier.document.service

import com.microsoft.portableIdentity.sdk.crypto.keyStore.IKeyStore
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.Endpoint
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.UserHubEndpoint
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IdentityHub") // serializer writes this value for "type"
class IdentityHubService(override val id: String, override val serviceEndpoint: String) :
    IdentifierDocumentService {
    override val type: String = "IdentityHub"

    companion object {
        fun create(
            id: String,
            signatureKeyRef: String,
            instances: List<Identifier>,
            keyStore: IKeyStore,
            logger: ILogger
        ): IdentifierDocumentService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw logger.error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdentityHubService(id, "https://testendpoint"
            )
        }
    }
}