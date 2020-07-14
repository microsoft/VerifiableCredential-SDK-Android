package com.microsoft.did.sdk.credential.service.models.attestations

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import kotlinx.serialization.Serializable

@Serializable
data class PresentationRequestBinding(
    var typeToSavedCards: Map<String, List<VerifiableCredentialHolder>>,
    var neededCards: List<PresentationAttestation>
)