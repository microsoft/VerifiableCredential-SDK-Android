// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.holders

import com.microsoft.did.sdk.credential.models.PortableIdentityCard
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

data class RequestedPresentationPicHolder(
    val portableIdentityCard: PortableIdentityCard,
    val presentationAttestation: PresentationAttestation
)