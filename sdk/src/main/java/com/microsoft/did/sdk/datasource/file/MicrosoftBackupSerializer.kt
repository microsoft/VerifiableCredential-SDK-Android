// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackupData2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.VcMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.BackupRestoreException
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

    suspend fun create(backupData: MicrosoftBackup2020): MicrosoftUnprotectedBackupData2020 {
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VcMetadata>()
        backupData.verifiableCredentials.forEach { verifiableCredentialMetadataPair ->
            vcMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.first.raw;
            vcMetaMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.second;
        }
        return MicrosoftUnprotectedBackupData2020(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backupData.walletMetadata,
            identifiers = rawIdentifierUtility.getAllIdentifiers()
        )
    }

    suspend fun import(backup: MicrosoftUnprotectedBackupData2020): MicrosoftBackup2020 {
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()

        backup.identifiers.forEach { raw ->
            val pair = rawIdentifierUtility.parseRawIdentifier(raw)
            identifiers.add(pair.first)
            keySet = keySet.union(pair.second)
        }

        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        return MicrosoftBackup2020(
            walletMetadata = backup.metaInf,
            verifiableCredentials = importVcs(backup)
        )
    }

    private fun importVcs(backup: MicrosoftUnprotectedBackupData2020): List<Pair<VerifiableCredential, VcMetadata>> {
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