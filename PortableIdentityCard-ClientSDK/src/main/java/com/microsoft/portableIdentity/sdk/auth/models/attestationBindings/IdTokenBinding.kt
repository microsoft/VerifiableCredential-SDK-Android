// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.models.attestationBindings

import com.microsoft.portableIdentity.sdk.auth.models.attestations.IdTokenAttestation

data class IdTokenBinding(
        val token: String,

        val configuration: String
)