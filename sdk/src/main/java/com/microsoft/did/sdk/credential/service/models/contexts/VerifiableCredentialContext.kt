package com.microsoft.did.sdk.credential.service.models.contexts

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

class VerifiableCredentialContext(
    val presentationAttestation: PresentationAttestation,
    val verifiableCredential: VerifiableCredential
)