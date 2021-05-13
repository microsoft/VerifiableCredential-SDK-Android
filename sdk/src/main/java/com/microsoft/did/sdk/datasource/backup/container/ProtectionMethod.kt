// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.backup.container

import com.microsoft.did.sdk.datasource.backup.content.ProtectedBackupData
import com.microsoft.did.sdk.datasource.backup.content.UnprotectedBackupData
import kotlinx.serialization.json.Json

abstract class ProtectionMethod {
    abstract fun wrap(unprotectedBackupData: UnprotectedBackupData, serializer: Json): ProtectedBackupData

    abstract fun unwrap(protectedBackupData: ProtectedBackupData, serializer: Json): UnprotectedBackupData
}