// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.RawIdentifierUtility
import com.microsoft.did.sdk.datasource.file.models.JweEncryptedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupOptions
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.BadPassword
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import com.microsoft.did.sdk.util.controlflow.andThen
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val microsoftBackupSerializer: MicrosoftBackupSerializer
) {
    suspend fun createBackup(options: JweProtectedBackupOptions): Result<JweProtectedBackup> {
        return when (options) {
            is PasswordProtectedBackupOptions -> {
                val backup = createUnprotectedBackup(options.unprotectedBackup)
                if (backup is Result.Success) {
                    jweBackupFactory.createPasswordBackup(backup.payload, options.password);
                } else {
                    backup as Result.Failure;
                }
            }
            else -> {
                Result.Failure(UnknownProtectionMethod("Unknown protection options: ${options::class.qualifiedName}"));
            }
        }
    }

    private suspend fun createUnprotectedBackup(options: UnprotectedBackupOptions): Result<UnprotectedBackup> {
        return when (options) {
            is MicrosoftUnprotectedBackupOptions -> {
                microsoftBackupSerializer.create(options)
            }
            else -> {
                Result.Failure(UnknownBackupFormat("Unknown backup options: ${options::class.qualifiedName}"));
             }
        }
    }

    suspend fun parseBackup(backupFile: InputStream): Result<JweProtectedBackup> {
        return jweBackupFactory.parseBackup(backupFile);
    }

    suspend fun restoreBackup(options: JweEncryptedBackupOptions): Result<UnprotectedBackupOptions> {
        return when (options) {
            is PasswordEncryptedBackupOptions -> {
                when (val backupAttempt = options.backup.decrypt(options.password)) {
                    is Result.Success -> importBackup(backupAttempt.payload)
                    is Result.Failure -> backupAttempt
                }
            }
            else -> {
                Result.Failure(UnknownBackupFormat("Unknown backup options: ${options::class.qualifiedName}"));
            }
        }
    }

    private suspend fun importBackup(backup: UnprotectedBackup): Result<UnprotectedBackupOptions> {
        return when (backup) {
            is MicrosoftUnprotectedBackup2020 -> {
                microsoftBackupSerializer.import(backup)
            }
            else -> {
                Result.Failure(UnknownBackupFormat("Unknown backup file: ${backup::class.qualifiedName}"))
            }
        }
    }
}