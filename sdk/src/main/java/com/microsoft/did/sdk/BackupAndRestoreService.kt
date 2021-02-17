// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.file.models.DifWordList
import com.microsoft.did.sdk.datasource.file.models.PASSWORD_SET_SIZE
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    context: Context
){
    val wordlist by lazy { DifWordList.getWordList(context) }

    fun generateDifPassword(): String {
        val random = SecureRandom()
        val wordSet = MutableList(PASSWORD_SET_SIZE) { "" }
        for (index in 0 until wordSet.count()) {
            var wordIndex: Int
            var word: String
            do {
                wordIndex = random.nextInt(wordlist.count())
                word = wordlist[wordIndex]
            } while (wordSet.contains(word))
            wordSet[index] = word
        }
        return wordSet.joinToString(" ")
    }

    suspend fun createPasswordBackup(
        password: String,
        walletMetadata: WalletMetadata,
        verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>,
        jsonSerializer: Json = UnprotectedBackup.serializer,
    ): PasswordProtectedBackup {
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VCMetadata>()
        val owningDids = mutableSetOf<String>()
        verifiableCredentials.forEach { vcPair ->
            val jti = vcPair.first.jti
            vcMap[jti] = vcPair.first.raw
            vcMetaMap[jti] = vcPair.second
            owningDids.add(vcPair.first.contents.sub)
        }
        val owningIds = owningDids.mapNotNull { did -> RawIdentity.didToRawIdentifier(did, identityRepository, keyStore) }
        return PasswordProtectedBackup(
            MicrosoftUnprotectedBackup2020(
                vcs = vcMap,
                vcsMetaInf = vcMetaMap,
                metaInf = walletMetadata,
                owningIds
            ),
            password,
            jsonSerializer,
        )
    }

    suspend fun parseJweBackup(input: InputStream,
                               jsonSerializer: Json = UnprotectedBackup.serializer): Result<JweProtectedBackup> {
        return JweProtectedBackup.parseBackup(input, jsonSerializer)
    }

    suspend fun importMicrosoftBackup(backup: MicrosoftUnprotectedBackup2020,
                                      walletMetadataCallback: suspend (WalletMetadata) -> Unit,
                                      verifiableCredentialCallback: suspend (VerifiableCredential, VCMetadata) -> Unit,
                                      listVerifiableCredentialCallback: suspend () -> List<String>,
                                      deleteVerifiableCredentialCallback: suspend (String) -> Unit,
                                      jsonSerializer: Json = Json.Default
                                      ): Result<Unit> {
        return backup.import(
            walletMetadataCallback,
            verifiableCredentialCallback,
            listVerifiableCredentialCallback,
            deleteVerifiableCredentialCallback,
            this.identityRepository,
            this.keyStore,
            jsonSerializer
        )
    }
}