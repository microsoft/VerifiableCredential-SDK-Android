package com.microsoft.portableIdentity.sdk.identifier.document.service

import com.microsoft.portableIdentity.sdk.crypto.keyStore.IKeyStore
import com.microsoft.portableIdentity.sdk.identifier.Id
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.Endpoint
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.UserHubEndpoint
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IdentityHub") // serializer writes this value for "type"
class IdHubService(override val id: String, val publicKey: String, @Polymorphic override val serviceEndpoint: Endpoint) :
    IdentifierDocService {
    override val type: String = "IdentityHub"

    companion object {
        fun create(
            id: String,
            signatureKeyRef: String,
            instances: List<Id>,
            keyStore: IKeyStore,
            logger: ILogger
        ): IdentifierDocService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw logger.error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdHubService(id, keyId.getLatestKeyId(),
                UserHubEndpoint(instances = didList)
            )
        }
    }
}