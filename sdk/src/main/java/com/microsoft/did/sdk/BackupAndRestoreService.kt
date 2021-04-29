// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.models.EncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020Data
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupData
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethodException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val microsoftBackupSerializer: MicrosoftBackupSerializer,
    private val serializer: Json
) {
    suspend fun createBackup(protectedBackupData: ProtectedBackupData): Result<JweProtectedBackup> {
        return runResultTry {
            val unprotectedBackupData = unwrapProtection(protectedBackupData)
            val unprotectedBackup = createUnprotectedBackup(unprotectedBackupData)
            Result.Success(protectBackup(unprotectedBackup, protectedBackupData))
        }
    }

    private fun unwrapProtection(protectedBackupData: ProtectedBackupData): UnprotectedBackupData {
        return when (protectedBackupData) {
            is PasswordProtectedBackupData -> protectedBackupData.unprotectedBackup
            else -> throw UnknownProtectionMethodException("Unknown protection options: ${protectedBackupData::class.qualifiedName}")
        }
    }

    private suspend fun createUnprotectedBackup(unprotectedBackupData: UnprotectedBackupData): UnprotectedBackup {
        return when (unprotectedBackupData) {
            is MicrosoftBackup2020Data -> microsoftBackupSerializer.create(unprotectedBackupData)
            else -> throw UnknownBackupFormatException("Unknown backup options: ${unprotectedBackupData::class.qualifiedName}");
        }
    }

    private fun protectBackup(unprotectedBackup: UnprotectedBackup, protectedBackupData: ProtectedBackupData): JweProtectedBackup {
        return when (protectedBackupData) {
            is PasswordProtectedBackupData -> jweBackupFactory.createPasswordBackup(unprotectedBackup, protectedBackupData.password)
            else -> throw UnknownProtectionMethodException("Unknown protection options: ${protectedBackupData::class.qualifiedName}")
        }
    }

    fun parseBackup(backupFile: InputStream): Result<JweProtectedBackup> {
        return Result.Success(jweBackupFactory.parseBackup(backupFile));
    }

    suspend fun restoreBackup(options: EncryptedBackupData): Result<UnprotectedBackupData> {
        return runResultTry {
            when (options) {
                is PasswordEncryptedBackupData -> {
                    val backupAttempt = options.backup.decrypt(options.password, serializer);
                    importBackup(backupAttempt)
                }
                else -> {
                    throw UnknownBackupFormatException("Unknown backup options: ${options::class.qualifiedName}");
                }
            }
        }
    }

    private suspend fun importBackup(backup: UnprotectedBackup): Result<UnprotectedBackupData> {
        return runResultTry {
            when (backup) {
                is MicrosoftUnprotectedBackup2020 -> {
                    Result.Success(microsoftBackupSerializer.import(backup))
                }
                else -> {
                    throw UnknownBackupFormatException("Unknown backup file: ${backup::class.qualifiedName}")
                }
            }
        }
    }
}