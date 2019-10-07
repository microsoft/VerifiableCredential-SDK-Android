package com.microsoft.did.sdk.identifier.document.service

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierDocumentService

class IdentityHubUserService private constructor (id: String, val publicKey: String, instances: List<String>):
    IdentifierDocumentService(id, "IdentityHub", UserHubEndpoint(instances)) {

    companion object {
        fun create(id: String, signatureKeyRef: String, instances: List<Identifier>, keyStore: IKeyStore): IdentityHubUserService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw Error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdentityHubUserService(id, keyId.getLatestKeyId(), didList)
        }
    }
}