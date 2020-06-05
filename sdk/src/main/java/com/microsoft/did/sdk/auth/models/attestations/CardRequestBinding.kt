package com.microsoft.did.sdk.auth.models.attestations

import com.microsoft.did.sdk.cards.PortableIdentityCard

data class CardRequestBinding(
    var typeToSavedCards: Map<String, List<PortableIdentityCard>>,
    var neededCards: List<PresentationAttestation>
)