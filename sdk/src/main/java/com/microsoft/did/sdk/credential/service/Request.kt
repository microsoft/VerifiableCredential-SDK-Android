/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationRequestBinding
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContent
import kotlinx.serialization.Serializable

@Serializable
sealed class Request(val attestations: CredentialAttestations?, val entityName: String = "", val entityIdentifier: String = "") {

    private var presentationBinding: PresentationRequestBinding? = null

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

@Serializable
class IssuanceRequest(val contract: VerifiableCredentialContract, val contractUrl: String) :
    Request(contract.input.attestations, contract.display.card.issuedBy, contract.input.issuer)

@Serializable
class PresentationRequest(val serializedToken: String, val content: OidcRequestContent) :
    Request(content.attestations, content.registration?.clientName ?: "", content.iss)