package com.microsoft.portableIdentity.sdk.auth.requests

import android.net.Uri
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent

sealed class Request(val attestations: CredentialAttestations?, val entityName: String = "", val entityIdentifier: String = "") {

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
class IssuanceRequest(val contract: PicContract, val contractUrl: String): Request(contract.input.attestations, contract.display.card.issuedBy, contract.input.issuer)
class PresentationRequest(val uri: Uri, val serializedToken: String, val content: OidcRequestContent) : Request(content.attestations, content.registration?.clientName ?: "", content.iss)