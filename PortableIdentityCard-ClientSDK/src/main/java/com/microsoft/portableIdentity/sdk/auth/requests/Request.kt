package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent

sealed class Request(val attestations: CredentialAttestations?) {

    private var presentationBinding: CardRequestBinding? = null

    fun getCredentialAttestations(): CredentialAttestations? {
        return attestations
    }

    fun hasPresentationAttestations(): Boolean {
        if (attestations?.presentations != null) {
            return true
        }
        return false
    }

    fun getPresentationAttestations(): List<PresentationAttestation> {
        val attestations = attestations?.presentations
        if (attestations != null) {
            return attestations
        }
        return emptyList()
    }
}

// Request can be either an Issuance or Presentation Request only.
class IssuanceRequest(val contract: PicContract, val contractUrl: String): Request(contract.input.attestations)
class PresentationRequest(val oidcParameters: Map<String, List<String>>, val serializedToken: String, val content: OidcRequestContent) : Request(content.attestations)