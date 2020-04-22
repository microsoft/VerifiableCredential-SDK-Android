package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.utilities.controlflow.PresentationException

sealed class CredentialRequest(val attestations: CredentialAttestations?) {

    // Credential Request can be either an Issuance or Presentation Request only.
    class IssuanceRequest(val contract: PicContract, val contractUrl: String): CredentialRequest(contract.input.attestations)
    class PresentationRequest(val oidcParameters: Map<String, List<String>>, val serializedToken: String, val contents: OidcRequestContent) : CredentialRequest(contents.attestations)

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
        throw PresentationException("No Presentation Attestations")
    }

    fun addPresentationBindings(binding: CardRequestBinding) {
        presentationBinding = binding
    }

    fun getPresentationBindings(): CardRequestBinding? {
        return presentationBinding
    }
}