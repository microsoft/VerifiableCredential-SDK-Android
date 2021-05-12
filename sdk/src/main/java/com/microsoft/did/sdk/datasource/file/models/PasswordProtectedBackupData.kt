// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

class PasswordProtectedBackupData(
    val password: String,
    override val unprotectedBackup2: UnprotectedBackup
) : ProtectedBackupData()