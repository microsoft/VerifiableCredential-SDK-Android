// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.BackupRestoreException
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Microsoft2020BackupProcessor @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierConverter: RawIdentifierConverter,
    private val jsonSerializer: Json
) {

    suspend fun transformToBackupData(backup: Microsoft2020Backup): Microsoft2020UnprotectedBackupData {
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VcMetadata>()
        backup.verifiableCredentials.forEach { verifiableCredentialMetadataPair ->
            vcMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.first.raw;
            vcMetaMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.second;
        }
        return Microsoft2020UnprotectedBackupData(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backup.walletMetadata,
            identifiers = rawIdentifierConverter.getAllIdentifiers()
        )
    }

    suspend fun import(backup: Microsoft2020UnprotectedBackupData): Microsoft2020Backup {
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()

        backup.identifiers.forEach { raw ->
            val pair = rawIdentifierConverter.parseRawIdentifier(raw)
            identifiers.add(pair.first)
            keySet = keySet.union(pair.second)
        }

        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        return Microsoft2020Backup(
            walletMetadata = backup.metaInf,
            verifiableCredentials = transformVcs(backup)
        )
    }

    private fun transformVcs(backup: Microsoft2020UnprotectedBackupData): List<Pair<VerifiableCredential, VcMetadata>> {
        val vcList = ArrayList<Pair<VerifiableCredential, VcMetadata>>()
        backup.vcs.forEach { mapEntry ->
            val (jti, rawVcToken) = mapEntry
            val jwsToken = JwsToken.deserialize(rawVcToken)
            val verifiableCredentialContent = jsonSerializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawVcToken, verifiableCredentialContent)
            if (backup.vcsMetaInf[jti] == null) throw BackupRestoreException("Corrupt backup. MetaInf for $jti is missing.")
            vcList.add(Pair(vc, backup.vcsMetaInf[jti]!!))
        }
        return vcList
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