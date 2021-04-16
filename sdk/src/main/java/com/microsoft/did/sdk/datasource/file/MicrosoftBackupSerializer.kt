// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020Data
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.MalformedIdentity
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MicrosoftBackupSerializer @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierUtility: RawIdentifierUtility,
    private val jsonSerializer: Json
) {

    suspend fun create(a2020Data: MicrosoftBackup2020Data): Result<MicrosoftUnprotectedBackup2020> {
        val vcMap = mutableMapOf<String, String>();
        val vcMetaMap = mutableMapOf<String, VCMetadata>();
        a2020Data.verifiableCredentials.forEach { verifiableCredentailMetadataPair ->
            vcMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.first.raw);
            vcMetaMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.second);
        }
        return Result.Success(MicrosoftUnprotectedBackup2020(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = options.walletMetadata,
            identifiers = rawIdentifierUtility.getAllIdentifiers()
        ))
    }

    suspend fun import(backup: MicrosoftUnprotectedBackup2020): Result<MicrosoftBackup2020Data> {
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()
        try {
            backup.identifiers.forEach { raw ->
                val pair = parseRawIdentifier(raw)
                identifiers.add(pair.first)
                keySet = keySet.union(pair.second)
            }
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(MalformedIdentity("unhandled exception thrown", exception))
        }
        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        return Result.Success(MicrosoftBackup2020Data(
            walletMetadata = backup.metaInf,
            verifiableCredentials = backup.vcsToIterator(jsonSerializer).asSequence().toList()
        ))
    }

    private fun parseRawIdentifier(
        identifierData: RawIdentity
    ): Pair<Identifier, Set<JWK>> {
        val updateKeyRef: String = identifierData.updateKey
        val recoveryKeyRef: String = identifierData.recoveryKey
        val keySet = rawIdentifierToKeySet(identifierData, updateKeyRef, recoveryKeyRef)
        if (updateKeyRef.isBlank() || recoveryKeyRef.isBlank()) {
            throw MalformedIdentity("update and recovery key required")
        }
        val excludeKeysForUse = listOf(updateKeyRef, recoveryKeyRef)
        val id = Identifier(
            identifierData.id,
            getKeyFromKeySet(KeyUse.SIGNATURE, keySet, excludeKeysForUse)?.keyID  ?: "",
            getKeyFromKeySet(KeyUse.ENCRYPTION, keySet, excludeKeysForUse)?.keyID ?: "",
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        return Pair(id, keySet)
    }

    private fun getKeyFromKeySet( keyUse: KeyUse, keySet: Set<JWK>, exclude: List<String>? = null): JWK? {
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
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
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

    private fun importKey(
        jwk: JWK,
        keyStore: EncryptedKeyStore
    ) {
        if (!keyStore.containsKey(jwk.keyID)) {
            keyStore.storeKey(jwk.keyID, jwk)
        }
    }
}