package com.microsoft.did.sdk.credential.service.models.attestations

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContainer

data class CardRequestBinding(
    var typeToSavedCards: Map<String, List<VerifiableCredentialContainer>>,
    var neededCards: List<PresentationAttestation>
)