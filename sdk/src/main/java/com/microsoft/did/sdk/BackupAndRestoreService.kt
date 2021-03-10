// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.RawIdentifierUtility
import com.microsoft.did.sdk.datasource.file.models.DifWordList
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.Constants.PASSWORD_SET_SIZE
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierUtility: RawIdentifierUtility,
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val jsonSerializer: Json = Json.Default,
    context: Context
) {
    val wordList by lazy { DifWordList.getWordList(context) }

    fun generateDifPassword(): String {
        val random = SecureRandom()
        val wordSet = MutableList(PASSWORD_SET_SIZE) { "" }
        for (index in 0 until wordSet.count()) {
            var wordIndex: Int
            var word: String
            do {
                wordIndex = random.nextInt(wordList.count())
                word = wordList[wordIndex]
            } while (wordSet.contains(word))
            wordSet[index] = word
        }
        return wordSet.joinToString(" ")
    }

    suspend fun createMicrosoftBackup(
        walletMetadata: WalletMetadata,
        verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>
    ): MicrosoftUnprotectedBackup2020 {
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VCMetadata>()
        val dids = rawIdentifierUtility.getAllIdentifiers()
        verifiableCredentials.forEach { vcPair ->
            val jti = vcPair.first.jti
            vcMap[jti] = vcPair.first.raw
            vcMetaMap[jti] = vcPair.second
        }
        return MicrosoftUnprotectedBackup2020(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = walletMetadata,
            dids
        )
    }

    suspend fun parseJweBackup(input: InputStream): Result<JweProtectedBackup> {
        return jweBackupFactory.parseBackup(input)
    }

    suspend fun importMicrosoftBackup(
        backup: MicrosoftUnprotectedBackup2020,
        walletMetadataCallback: suspend (WalletMetadata) -> Unit,
        verifiableCredentialCallback: suspend (VerifiableCredential, VCMetadata) -> Unit,
        listVerifiableCredentialCallback: suspend () -> List<String>,
        deleteVerifiableCredentialCallback: suspend (String) -> Unit
    ): Result<Unit> {
        return backup.import(
            walletMetadataCallback,
            verifiableCredentialCallback,
            listVerifiableCredentialCallback,
            deleteVerifiableCredentialCallback,
            this.identityRepository,
            this.keyStore
        )
    }
}