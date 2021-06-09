// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content

import kotlinx.serialization.Serializable

/**
 * BackupData classes represent the data as found in the backup file.
 * On the other hand, Backup classes represent the same Backups but imported into the class model of the SDK.
 */
@Serializable
abstract class UnprotectedBackupData {
    abstract val type: String
}
