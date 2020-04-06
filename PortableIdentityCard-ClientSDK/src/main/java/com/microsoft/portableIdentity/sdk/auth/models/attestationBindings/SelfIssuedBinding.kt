// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.models.attestationBindings

import com.microsoft.portableIdentity.sdk.auth.models.attestations.SelfIssuedAttestation
import com.microsoft.portableIdentity.sdk.cards.SelfIssued

data class SelfIssuedBinding(
        val claim: String,

        val attestation: String
)