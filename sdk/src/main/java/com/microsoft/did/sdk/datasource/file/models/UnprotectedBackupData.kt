// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * BackupData classes represent the data as found in the backup file. It is a serialized version of the file.
 * Backup classes represent the same Backups but imported into the class model of the SDK.
 */
@Serializable
abstract class UnprotectedBackupData {
    abstract val type: String
}
