// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.model

import com.microsoft.did.sdk.identifier.models.Identifier
import kotlinx.serialization.Serializable

@Serializable
data class BackupContent(
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, Map<String, String>>,
    val identifier: List<Identifier>
)