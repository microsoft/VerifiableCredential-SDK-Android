// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.model

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier

data class BackupData(
    val vcs: Map<VerifiableCredential, Map<String, String>>,
    val identifiers: List<Identifier>
)