// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.models.EncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackupData2020
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
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

    private fun unwrapProtection(protectedBackupData: ProtectedBackupData): UnprotectedBackup {
        return when (protectedBackupData) {
            is PasswordProtectedBackupData -> protectedBackupData.unprotectedBackup
            else -> throw UnknownProtectionMethodException("Unknown protection options: ${protectedBackupData::class.qualifiedName}")
        }
    }

    private suspend fun createUnprotectedBackup(unprotectedBackup: UnprotectedBackup): UnprotectedBackupData {
        return when (unprotectedBackup) {
            is MicrosoftBackup2020 -> microsoftBackupSerializer.create(unprotectedBackup)
            else -> throw UnknownBackupFormatException("Unknown backup options: ${unprotectedBackup::class.qualifiedName}");
        }
    }

    private fun protectBackup(unprotectedBackupData: UnprotectedBackupData, protectedBackupData: ProtectedBackupData): JweProtectedBackup {
        return when (protectedBackupData) {
            is PasswordProtectedBackupData -> jweBackupFactory.createPasswordBackup(unprotectedBackupData, protectedBackupData.password)
            else -> throw UnknownProtectionMethodException("Unknown protection options: ${protectedBackupData::class.qualifiedName}")
        }
    }

    fun parseBackup(backupFile: InputStream): Result<JweProtectedBackup> {
        return Result.Success(jweBackupFactory.parseBackup(backupFile));
    }

    suspend fun restoreBackup(backupData: EncryptedBackupData): Result<UnprotectedBackup> {
        return runResultTry {
            val unprotectedBackup = decryptBackup(backupData)
            val unprotectedBackupData = importBackup(unprotectedBackup)
            Result.Success(unprotectedBackupData)
        }
    }

    private fun decryptBackup(backupData: EncryptedBackupData): UnprotectedBackupData {
        return when (backupData) {
            is PasswordEncryptedBackupData -> backupData.backup.decrypt(backupData.password, serializer)
            else -> throw UnknownBackupFormatException("Unknown backup options: ${backupData::class.qualifiedName}")
        }
    }

    private suspend fun importBackup(backupData: UnprotectedBackupData): UnprotectedBackup {
        return when (backupData) {
            is MicrosoftUnprotectedBackupData2020 -> microsoftBackupSerializer.import(backupData)
            else -> throw UnknownBackupFormatException("Unknown backup file: ${backupData::class.qualifiedName}")
        }
    }
}