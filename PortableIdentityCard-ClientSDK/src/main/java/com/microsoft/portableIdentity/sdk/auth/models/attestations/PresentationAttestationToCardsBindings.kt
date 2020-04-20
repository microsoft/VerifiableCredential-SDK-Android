package com.microsoft.portableIdentity.sdk.auth.models.attestations

import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard

data class PresentationAttestationToCardsBindings(
    var typeToSavedCards: Map<String, List<PortableIdentityCard>>,
    var neededCards: List<PresentationAttestation>
)