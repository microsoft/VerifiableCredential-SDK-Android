// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.backup.BackupParser
import com.microsoft.did.sdk.backup.UnprotectedBackup
import com.microsoft.did.sdk.backup.container.ProtectionMethod
import com.microsoft.did.sdk.backup.content.BackupProcessorFactory
import com.microsoft.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    private val backupParser: BackupParser,
    private val backupProcessorFactory: BackupProcessorFactory,
    private val identityRepository: IdentifierRepository,
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
    suspend fun exportBackup(unprotectedBackup: UnprotectedBackup, protectionMethod: ProtectionMethod): Result<ProtectedBackupData> {
        return runResultTry {
            val unprotectedBackupData = backupProcessorFactory.export(unprotectedBackup)
            val protectedBackupData = protectionMethod.wrap(unprotectedBackupData, serializer)
            Result.Success(protectedBackupData)
        }
    }

    /**
     * ProtectedBackupData is decrypted and transformed into an UnprotectedBackup.
     *
     * All identifiers in the SDK database are cleared before starting the import!
     * During this process all contained Identifiers and keys are restored into the SDKs database.
     * Everything else (3rd party metadata) is contained in the UnprotectedBackup and IS NOT restored.
     *
     * @param protectedBackupData the contents are unwraped and returned
     * @param protectionMethod the type and contents of this parameter determines the protection method used to unwrap and decrypt the backup
     * @return the transformed and decrypted backup of the type found within protectedBackupData
     */
    suspend fun importBackup(protectedBackupData: ProtectedBackupData, protectionMethod: ProtectionMethod): Result<UnprotectedBackup> {
        return runResultTry {
            identityRepository.deleteAll()
            val unprotectedBackupData = protectionMethod.unwrap(protectedBackupData, serializer)
            val unprotectedBackup = backupProcessorFactory.import(unprotectedBackupData)
            Result.Success(unprotectedBackup)
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
}