package com.microsoft.did.sdk.credential.service.models.contexts

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation

data class IdTokenContext (
    val idTokenAttestation: IdTokenAttestation,
    val rawToken: String
)