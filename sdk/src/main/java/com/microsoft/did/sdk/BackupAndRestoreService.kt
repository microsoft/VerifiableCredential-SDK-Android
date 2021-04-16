// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.models.EncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020Data
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupData
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val microsoftBackupSerializer: MicrosoftBackupSerializer
) {
    suspend fun createBackup(options: ProtectedBackupData): Result<JweProtectedBackup> {
        return when (options) {
            is PasswordProtectedBackupData -> {
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

    private suspend fun createUnprotectedBackup(options: UnprotectedBackupData): Result<UnprotectedBackup> {
        return when (options) {
            is MicrosoftBackup2020Data -> {
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

    suspend fun restoreBackup(options: EncryptedBackupData): Result<UnprotectedBackupData> {
        return when (options) {
            is PasswordEncryptedBackupData -> {
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

    private suspend fun importBackup(backup: UnprotectedBackup): Result<UnprotectedBackupData> {
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