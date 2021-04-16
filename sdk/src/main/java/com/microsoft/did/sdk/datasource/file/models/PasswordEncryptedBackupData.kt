// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

class PasswordEncryptedBackupData constructor(
    val password: String,
    override val backup: PasswordProtectedBackup
) : EncryptedBackupData()