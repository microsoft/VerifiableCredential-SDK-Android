// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.backup.BackupParser
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020BackupProcessor
import com.microsoft.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020Backup
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.backup.container.ProtectionMethod
import com.microsoft.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.did.sdk.backup.UnprotectedBackup
import com.microsoft.did.sdk.backup.content.BackupProcessorFactory
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAndRestoreService @Inject constructor(
    private val backupParser: BackupParser,
    private val backupProcessorFactory: BackupProcessorFactory,
    private val serializer: Json
) {

    /**
     * Transforms and encrypts the backup into ProtectedBackupData which can be written to a file.
     *
     * Data about Identifiers and keys are only available within the SDK and as such they are automatically added here
     * without being exposed outside of the SDK. Therefore the returned ProtectedBackupData will contain more data
     * than what is passed via unprotectedBackup.
     *
     * @param unprotectedBackup the type of this parameter determines the contents of the returned ProtectedBackupData
     * @param protectionMethod the type of this parameter determines the protection method applied
     * @return content of the backup ready to be written to a file
     */
    suspend fun createBackup(unprotectedBackup: UnprotectedBackup, protectionMethod: ProtectionMethod): Result<ProtectedBackupData> {
        return runResultTry {
            val unprotectedBackupData = backupProcessorFactory.export(unprotectedBackup)
            val protectedBackup = protectionMethod.wrap(unprotectedBackupData, serializer)
            Result.Success(protectedBackup)
        }
    }

    /**
     * Given a serialized backup tries to determine the backup type and deserialize it.
     *
     * @param backup serialized backup
     * @return parsed backup
     */
    suspend fun parseBackup(backup: String): Result<ProtectedBackupData> {
        return runResultTry {
            Result.Success(backupParser.parseBackup(backup))
        }
    }

    /**
     * ProtectedBackupData is decrypted and transformed into an UnprotectedBackup.
     *
     * During this process all contained Identifiers and keys are restored into the SDKs database!
     * Everything else is contained in the UnprotectedBackup and IS NOT restored.
     *
     * @param protectedBackupData a protected method that will be transformed and decrypted according to it's type
     * @param protectionMethod used to decrypt the passed backup and has to fit the way the it's protection method
     * @return the transformed and decrypted backup of the type found within protectedBackupData
     */
    suspend fun restoreBackup(protectedBackupData: ProtectedBackupData, protectionMethod: ProtectionMethod): Result<UnprotectedBackup> {
        return runResultTry {
            val unprotectedBackup = protectionMethod.unwrap(protectedBackupData, serializer)
            val unprotectedBackupData = backupProcessorFactory.import(unprotectedBackup)
            Result.Success(unprotectedBackupData)
        }
    }
}