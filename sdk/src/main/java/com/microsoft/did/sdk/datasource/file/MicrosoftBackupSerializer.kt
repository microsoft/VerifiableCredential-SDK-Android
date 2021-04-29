// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020Data
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.MalformedIdentityException
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicrosoftBackupSerializer @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierUtility: RawIdentifierUtility,
    private val jsonSerializer: Json
) {

    suspend fun create(backupData: MicrosoftBackup2020Data): MicrosoftUnprotectedBackup2020 {
        val vcMap = mutableMapOf<String, String>();
        val vcMetaMap = mutableMapOf<String, VCMetadata>();
        backupData.verifiableCredentials.forEach { verifiableCredentailMetadataPair ->
            vcMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.first.raw);
            vcMetaMap.put(verifiableCredentailMetadataPair.first.jti, verifiableCredentailMetadataPair.second);
        }
        return MicrosoftUnprotectedBackup2020(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backupData.walletMetadata,
            identifiers = rawIdentifierUtility.getAllIdentifiers()
        )
    }

    suspend fun import(backup: MicrosoftUnprotectedBackup2020): MicrosoftBackup2020Data {
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()
        try {
            backup.identifiers.forEach { raw ->
                val pair = rawIdentifierUtility.parseRawIdentifier(raw)
                identifiers.add(pair.first)
                keySet = keySet.union(pair.second)
            }
        } catch (exception: SdkException) {
            throw exception
        } catch (exception: Exception) {
            throw MalformedIdentityException("unhandled exception thrown", exception)
        }
        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        return MicrosoftBackup2020Data(
            walletMetadata = backup.metaInf,
            verifiableCredentials = backup.vcsToIterator(jsonSerializer).asSequence().toList()
        )
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