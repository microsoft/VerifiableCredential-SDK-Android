// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models.microsoft2020

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup

class Microsoft2020Backup(
    val walletMetadata: WalletMetadata,
    val verifiableCredentials: List<Pair<VerifiableCredential, VcMetadata>>
) : UnprotectedBackup()