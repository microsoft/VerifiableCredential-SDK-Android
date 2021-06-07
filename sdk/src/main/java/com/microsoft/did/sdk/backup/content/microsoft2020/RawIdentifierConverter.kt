// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.MalformedIdentityException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RawIdentifierConverter @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore
) {

    suspend fun getAllIdentifiers(): List<RawIdentity> {
        return identityRepository.queryAllLocal().map { did -> createRawIdentifier(did) }
    }

    fun parseRawIdentifier(
        identifierData: RawIdentity
    ): Pair<Identifier, Set<JWK>> {
        val updateKeyRef = identifierData.updateKey
        val recoveryKeyRef = identifierData.recoveryKey
        val keySet = rawIdentifierToKeySet(identifierData, updateKeyRef, recoveryKeyRef)
        if (updateKeyRef.isBlank() || recoveryKeyRef.isBlank()) {
            throw MalformedIdentityException("update and recovery key required")
        }
        val excludeKeysForUse = listOf(updateKeyRef, recoveryKeyRef)
        val id = Identifier(
            identifierData.id,
            getKeyFromKeySet(KeyUse.SIGNATURE, keySet, excludeKeysForUse)?.keyID ?: "",
            getKeyFromKeySet(KeyUse.ENCRYPTION, keySet, excludeKeysForUse)?.keyID ?: "",
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        return Pair(id, keySet)
    }

    private fun getKeyFromKeySet(keyUse: KeyUse, keySet: Set<JWK>, exclude: List<String>? = null): JWK? {
        return when (keyUse) {
            KeyUse.SIGNATURE -> {
                keySet.firstOrNull { keyIsSigning(it) && !(exclude?.contains(it.keyID) ?: false) }
            }
            KeyUse.ENCRYPTION -> {
                keySet.firstOrNull { keyIsEncrypting(it) && !(exclude?.contains(it.keyID) ?: false) }
            }
            else -> {
                null
            }
        }
    }

    private fun rawIdentifierToKeySet(identifierData: RawIdentity, updateKeyRef: String, recoveryKeyRef: String): Set<JWK> {
        var signingKeyRef = ""
        var encryptingKeyRef = ""
        val keySet = mutableSetOf<JWK>()
        for (key in identifierData.keys) {
            if (signingKeyRef.isBlank() && keyIsSigning(key)) {
                signingKeyRef = getKidFromJWK(key)
                keySet.add(key)
            } else if (encryptingKeyRef.isBlank() && keyIsEncrypting(key)) {
                encryptingKeyRef = getKidFromJWK(key)
                keySet.add(key)
            } else if (key.keyID == updateKeyRef) {
                getKidFromJWK(key)
                keySet.add(key)
            } else if (key.keyID == recoveryKeyRef) {
                getKidFromJWK(key)
                keySet.add(key)
            }
        }
        return keySet
    }

    private fun keyIsSigning(key: JWK): Boolean {
        return key.keyOperations?.any { listOf(KeyOperation.SIGN, KeyOperation.VERIFY).contains(it) } == true ||
            key.keyUse == KeyUse.SIGNATURE
    }

    private fun keyIsEncrypting(key: JWK): Boolean {
        return key.keyOperations?.any { listOf(KeyOperation.ENCRYPT, KeyOperation.DECRYPT).contains(it) } == true ||
            key.keyOperations?.any { listOf(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY).contains(it) } == true ||
            key.keyUse == KeyUse.ENCRYPTION
    }

    private fun getKidFromJWK(jwk: JWK): String {
        return jwk.keyID ?: throw KeyException("Imported JWK has no key id.")
    }

    private fun createRawIdentifier(identity: Identifier): RawIdentity {
        return RawIdentity(
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