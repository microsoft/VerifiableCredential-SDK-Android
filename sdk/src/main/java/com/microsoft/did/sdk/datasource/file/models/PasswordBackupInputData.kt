// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

class PasswordBackupInputData(
    val password: String,
    override val unprotectedBackup: UnprotectedBackup
) : BackupInputData()