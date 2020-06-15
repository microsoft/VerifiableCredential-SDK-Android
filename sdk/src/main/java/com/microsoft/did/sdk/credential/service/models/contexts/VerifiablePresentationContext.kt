// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.contexts

import com.microsoft.did.sdk.credential.models.PortableIdentityCard
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

data class VerifiablePresentationContext(
    val portableIdentityCard: PortableIdentityCard,
    val presentationAttestation: PresentationAttestation
)