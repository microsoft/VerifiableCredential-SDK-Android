package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.utilities.controlflow.PresentationException

abstract class CredentialRequest(val attestations: CredentialAttestations?): Request {

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