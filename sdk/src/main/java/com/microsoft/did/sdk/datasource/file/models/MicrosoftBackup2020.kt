// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential

class MicrosoftBackup2020(
    val walletMetadata: WalletMetadata,
    val verifiableCredentials: List<Pair<VerifiableCredential, VcMetadata>>
) : UnprotectedBackup()