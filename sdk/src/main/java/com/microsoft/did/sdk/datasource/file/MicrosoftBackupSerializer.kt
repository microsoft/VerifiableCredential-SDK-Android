// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.MalformedIdentity
import com.microsoft.did.sdk.util.controlflow.MalformedMetadata
import com.microsoft.did.sdk.util.controlflow.MalformedVerifiableCredential
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

    suspend fun create(options: MicrosoftUnprotectedBackupOptions): Result<MicrosoftUnprotectedBackup2020> {
        val vcMap = mutableMapOf<String, String>();
        val vcMetaMap = mutableMapOf<String, VCMetadata>();
        options.verifiableCredentials.forEach { verifiableCredentailMetadataPair ->
            vcMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.first.raw);
            vcMetaMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.second);
        }
        return Result.Success(MicrosoftUnprotectedBackup2020(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = options.meta,
            identifiers = rawIdentifierUtility.getAllIdentifiers()
        ))
    }

    suspend fun import(backup: MicrosoftUnprotectedBackup2020): Result<MicrosoftUnprotectedBackupOptions> {
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

        return Result.Success(MicrosoftUnprotectedBackupOptions(
            walletMetadata = backup.metaInf,
            verifiableCredentials = backup.vcsToIterator(jsonSerializer).asSequence().toList()
        ))
    }

    private fun parseRawIdentifier(
        identifierData: RawIdentity
    ): Pair<Identifier, Set<JWK>> {
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
        val recoveryKeyRef: String = identifierData.recoveryKey
        val updateKeyRef: String = identifierData.updateKey
        val keySet = mutableSetOf<JWK>()
        for (key in identifierData.keys) {
            if (signingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.SIGN, KeyOperation.VERIFY).contains(it) } == true ||
                    key.keyUse == KeyUse.SIGNATURE)
            ) {
                signingKeyRef = getKidFromJWK(key)
                keySet.add(key)
            } else if (encryptingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.ENCRYPT, KeyOperation.DECRYPT).contains(it) } == true ||
                    key.keyOperations?.any { listOf(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY).contains(it) } == true ||
                    key.keyUse == KeyUse.ENCRYPTION)
            ) {
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
        if (updateKeyRef.isBlank() || recoveryKeyRef.isBlank()) {
            throw MalformedIdentity("update and recovery key required")
        }
        val id = Identifier(
            identifierData.id,
            signingKeyRef,
            encryptingKeyRef,
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        return Pair(id, keySet)
    }

    private fun getKidFromJWK(jwk: JWK): String {
        return jwk.keyID ?: throw KeyException("Imported JWK has no key id.")
    }

    private fun importKey(
        jwk: JWK,
        keyStore: EncryptedKeyStore
    ) {
        if (!keyStore.containsKey(jwk.keyID)) {
            keyStore.storeKey(jwk, jwk.keyID)
        }
    }
}