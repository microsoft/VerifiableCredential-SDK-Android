// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.backup.UnprotectedBackup
import com.microsoft.did.sdk.credential.models.VerifiableCredential

data class Microsoft2020UnprotectedBackup(
    val walletMetadata: WalletMetadata,
    val verifiableCredentials: List<Pair<VerifiableCredential, VcMetadata>>
) : UnprotectedBackup()