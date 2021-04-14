// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

class PasswordProtectedBackupOptions(
    val password: String,
    override val unprotectedBackup: UnprotectedBackupOptions
) : JweProtectedBackupOptions()