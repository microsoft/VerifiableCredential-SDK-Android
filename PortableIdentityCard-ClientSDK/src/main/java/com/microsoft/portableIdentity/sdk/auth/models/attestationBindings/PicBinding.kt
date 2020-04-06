// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.models.attestationBindings

import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential

data class PicBinding(
        val card: PortableIdentityCard,

        val type: String
)