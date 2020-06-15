package com.microsoft.did.sdk.credential.service.models.attestations

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder

data class PresentationRequestBinding(
    var typeToSavedCards: Map<String, List<VerifiableCredentialHolder>>,
    var neededCards: List<PresentationAttestation>
)