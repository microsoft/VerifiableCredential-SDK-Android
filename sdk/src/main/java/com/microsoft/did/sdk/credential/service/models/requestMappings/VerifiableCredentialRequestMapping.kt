package com.microsoft.did.sdk.credential.service.models.requestMappings

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

class VerifiableCredentialRequestMapping(
    val presentationAttestation: PresentationAttestation,
    val verifiableCredential: VerifiableCredential
)