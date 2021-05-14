// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content

import com.microsoft.did.sdk.backup.UnprotectedBackup
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020BackupProcessor
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackup
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupProcessorFactory @Inject constructor(
    private val microsoft2020BackupProcessor: Microsoft2020BackupProcessor
) : BackupProcessor {

    private fun getProcessor(unprotectedBackup: UnprotectedBackup): BackupProcessor {
        return when (unprotectedBackup) {
            is Microsoft2020UnprotectedBackup -> microsoft2020BackupProcessor
            else -> throw UnknownBackupFormatException("Unknown backup type: ${unprotectedBackup::class.qualifiedName}")
        }
    }

    private fun getProcessor(backupData: UnprotectedBackupData): BackupProcessor {
        return when (backupData) {
            is Microsoft2020UnprotectedBackupData -> microsoft2020BackupProcessor
            else -> throw UnknownBackupFormatException("Unknown backupData type: ${backupData::class.qualifiedName}")
        }
    }

    override suspend fun export(backup: UnprotectedBackup): UnprotectedBackupData {
        return getProcessor(backup).export(backup)
    }

    override suspend fun import(backupData: UnprotectedBackupData): UnprotectedBackup {
        return getProcessor(backupData).import(backupData)
    }
}