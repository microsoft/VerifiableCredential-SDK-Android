// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.IdentifierService
import com.microsoft.did.sdk.backup.UnprotectedBackup
import com.microsoft.did.sdk.backup.content.BackupProcessor
import com.microsoft.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.BackupException
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Microsoft2020BackupProcessor @Inject constructor(
    private val identifierService: IdentifierService,
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierConverter: RawIdentifierConverter,
    private val jsonSerializer: Json
) : BackupProcessor {

    override suspend fun export(backup: UnprotectedBackup): UnprotectedBackupData {
        if (backup !is Microsoft2020UnprotectedBackup) throw BackupException("Backup has wrong type ${backup::class.simpleName}")
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VcMetadata>()
        backup.verifiableCredentials.forEach { verifiableCredentialMetadataPair ->
            vcMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.first.raw
            vcMetaMap[verifiableCredentialMetadataPair.first.jti] = verifiableCredentialMetadataPair.second
        }

/*      This line creates master DID and its key if there isn't one already which is required for export.
        The created key is retrieved from keystore and used as seed in wallet metadata below.*/
        identifierService.getMasterIdentifier()
        backup.walletMetadata.seed = keyStore.getKey(Constants.MAIN_IDENTIFIER_REFERENCE).toJSONString()
        return Microsoft2020UnprotectedBackupData(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backup.walletMetadata,
            identifiers = rawIdentifierConverter.getAllIdentifiers()
        )
    }

    override suspend fun import(backupData: UnprotectedBackupData): UnprotectedBackup {
        if (backupData !is Microsoft2020UnprotectedBackupData) throw BackupException("BackupData has wrong type ${backupData::class.simpleName}")
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()

        backupData.identifiers.forEach { raw ->
            val pair = rawIdentifierConverter.parseRawIdentifier(raw)
            identifiers.add(pair.first)
            keySet = keySet.union(pair.second)
        }

        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        keyStore.storeKey(Constants.MAIN_IDENTIFIER_REFERENCE, JWK.parse(backupData.metaInf.seed))
        return Microsoft2020UnprotectedBackup(
            walletMetadata = backupData.metaInf,
            verifiableCredentials = transformVcs(backupData)
        )
    }

    private fun transformVcs(backup: Microsoft2020UnprotectedBackupData): List<Pair<VerifiableCredential, VcMetadata>> {
        val vcList = ArrayList<Pair<VerifiableCredential, VcMetadata>>()
        backup.vcs.forEach { mapEntry ->
            val (jti, rawVcToken) = mapEntry
            val jwsToken = JwsToken.deserialize(rawVcToken)
            val verifiableCredentialContent = jsonSerializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawVcToken, verifiableCredentialContent)
            if (backup.vcsMetaInf[jti] == null) throw BackupException("Corrupt backup. MetaInf for $jti is missing.")
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