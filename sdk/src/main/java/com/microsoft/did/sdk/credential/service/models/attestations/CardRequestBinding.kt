package com.microsoft.did.sdk.credential.service.models.attestations

import com.microsoft.did.sdk.credential.models.PortableIdentityCard

data class CardRequestBinding(
    var typeToSavedCards: Map<String, List<PortableIdentityCard>>,
    var neededCards: List<PresentationAttestation>
)