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
import com.microsoft.did.sdk.util.controlflow.BadPassword
import com.microsoft.did.sdk.util.controlflow.IoFailure
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import com.microsoft.did.sdk.util.controlflow.andThen
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val jweBackupFactory: JweProtectedBackupFactory,
    private val microsoftBackupSerializer: MicrosoftBackupSerializer,
    private val serializer: Json
) {
    fun writeBackup(backup: JweProtectedBackup, output: OutputStream): Result<Unit> {
        return try {
            jweBackupFactory.writeOutput(backup, output)
            return Result.Success(Unit)
        } catch (exception: IOException) {
            Result.Failure(IoFailure("Failed to write backup", exception))
        }
    }

    suspend fun createBackup(options: ProtectedBackupData): Result<JweProtectedBackup> {
        return when (options) {
            is PasswordProtectedBackupData -> {
                return createUnprotectedBackup(options.unprotectedBackup).andThen {
                    backup ->
                    Result.Success(jweBackupFactory.createPasswordBackup(backup, options.password));
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
                Result.Success(microsoftBackupSerializer.create(options))
            }
            else -> {
                Result.Failure(UnknownBackupFormat("Unknown backup options: ${options::class.qualifiedName}"));
            }
        }
    }

    fun parseBackup(backupFile: InputStream): Result<JweProtectedBackup> {
        return Result.Success(jweBackupFactory.parseBackup(backupFile));
    }

    suspend fun restoreBackup(options: EncryptedBackupData): Result<UnprotectedBackupData> {
        return when (options) {
            is PasswordEncryptedBackupData -> {
                val backupAttempt = options.backup.decrypt(options.password, serializer);
                if (backupAttempt != null) {
                    importBackup(backupAttempt)
                } else {
                    Result.Failure(BadPassword("Failed to decrypt"))
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
                Result.Success(microsoftBackupSerializer.import(backup))
            }
            else -> {
                Result.Failure(UnknownBackupFormat("Unknown backup file: ${backup::class.qualifiedName}"))
            }
        }
    }
}