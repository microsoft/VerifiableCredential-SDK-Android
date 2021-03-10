// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.jwk.JWK
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RawIdentifierUtility @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore
) {

    suspend fun getAllIdentifiers(): List<RawIdentity> {
        return identityRepository.queryAllLocal().mapNotNull { did -> this.createRawIdentifier(did) }
    }

    private fun createRawIdentifier(identity: Identifier): RawIdentity? {
        RawIdentity(
            id = identity.id,
            name = identity.name,
            keys = getIdentifierKeys(identity),
            updateKey = identity.updateKeyReference,
            recoveryKey = identity.recoveryKeyReference
        )
    }

    private fun getIdentifierKeys(identity: Identifier): List<JWK> {
        return listOf(
            identity.encryptionKeyReference,
            identity.signatureKeyReference,
            identity.updateKeyReference,
            identity.recoveryKeyReference
        ).mapNotNull { keyId ->
            if (keyId.isNotBlank()) {
                keyStore.getKey(keyId)
            } else {
                null
            }
        }
    }
}