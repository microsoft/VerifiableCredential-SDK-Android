// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import kotlinx.serialization.json.Json

abstract class ProtectionMethod {
    abstract fun encrypt(unprotectedBackupData: UnprotectedBackupData, serializer: Json): ProtectedBackupData

    abstract fun decrypt(protectedBackupData: ProtectedBackupData, serializer: Json): UnprotectedBackupData
}