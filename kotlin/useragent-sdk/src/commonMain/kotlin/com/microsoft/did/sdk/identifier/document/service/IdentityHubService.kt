package com.microsoft.did.sdk.identifier.document.service

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierDocumentService

class IdentityHubService constructor (id: String, val publicKey: String, endpoint: Endpoint):
    IdentifierDocumentService(id, "IdentityHub", endpoint) {

    companion object {
        fun create(id: String, signatureKeyRef: String, instances: List<Identifier>, keyStore: IKeyStore): IdentityHubService {
            val keyId = keyStore.list()[signatureKeyRef] ?: throw Error("Could not find key $signatureKeyRef")
            val didList = instances.map { it.document.id }
            return IdentityHubService(id, keyId.getLatestKeyId(), UserHubEndpoint(didList))
        }
    }
}