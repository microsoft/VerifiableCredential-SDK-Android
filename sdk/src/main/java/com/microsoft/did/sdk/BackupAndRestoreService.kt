// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.file.models.DifWordList
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(

    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
){
    companion object {
        fun getWordList(): List<String> {
            return DifWordList.wordList
        }
    }

    suspend fun createPasswordBackup(
        walletMetadata: WalletMetadata,
        verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>,
        jsonSerializer: Json = UnprotectedBackup.serializer,
    ): PasswordProtectedBackup {
        return PasswordProtectedBackup.wrap(
            MicrosoftUnprotectedBackup2020.build(
                metadata = walletMetadata,
                verifiableCredentials = verifiableCredentials,
                identityRepository, keyStore
            ),
            jsonSerializer
        )
    }

    suspend fun parseBackup(input: InputStream): JweProtectedBackup {
        return JweProtectedBackup.parseBackup(input)
    }
}