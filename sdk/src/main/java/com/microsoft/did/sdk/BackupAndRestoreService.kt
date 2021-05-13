// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.models.EncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020Backup
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordBackupInputData
import com.microsoft.did.sdk.datasource.file.models.BackupInputData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethodException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val microsoftBackupSerializer: MicrosoftBackupSerializer,
    private val serializer: Json
) {
    suspend fun createBackup(backupInputData: BackupInputData): Result<ProtectedBackup> {
        return runResultTry {
            val unprotectedBackup = createUnprotectedBackup(backupInputData.unprotectedBackup)
            Result.Success(protectBackup(unprotectedBackup, backupInputData))
        }
    }

    private suspend fun createUnprotectedBackup(unprotectedBackup: UnprotectedBackup): UnprotectedBackupData {
        return when (unprotectedBackup) {
            is Microsoft2020Backup -> microsoftBackupSerializer.create(unprotectedBackup)
            else -> throw UnknownBackupFormatException("Unknown backup options: ${unprotectedBackup::class.qualifiedName}");
        }
    }

    private fun protectBackup(unprotectedBackupData: UnprotectedBackupData, backupInputData: BackupInputData): ProtectedBackup {
        return when (backupInputData) {
            is PasswordBackupInputData -> jweBackupFactory.createPasswordBackup(unprotectedBackupData, backupInputData.password)
            else -> throw UnknownProtectionMethodException("Unknown protection options: ${backupInputData::class.qualifiedName}")
        }
    }

    fun parseBackup(backup: String): Result<ProtectedBackup> {
        return Result.Success(jweBackupFactory.parseBackup(backup))
    }

    suspend fun restoreBackup(protectedBackup: ProtectedBackup, backupData: EncryptedBackupData): Result<UnprotectedBackup> {
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
            is Microsoft2020UnprotectedBackupData -> microsoftBackupSerializer.import(backupData)
            else -> throw UnknownBackupFormatException("Unknown backup file: ${backupData::class.qualifiedName}")
        }
    }
}