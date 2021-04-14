// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential

class MicrosoftUnprotectedBackupOptions (
    val walletMetadata: WalletMetadata,
    val verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>
) : UnprotectedBackupOptions()