/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import kotlinx.serialization.Serializable

@Serializable
sealed class Request(val entityName: String, val entityIdentifier: String)

@Serializable
class IssuanceRequest(val contract: VerifiableCredentialContract, val contractUrl: String) :
    Request(contract.display.card.issuedBy, contract.input.issuer) {
    fun getAttestations(): CredentialAttestations {
        return contract.input.attestations
    }
}

@Serializable
class PresentationRequest(val content: PresentationRequestContent) :
    Request(content.registration.clientName, content.issuer) {
    fun getPresentationDefinition(): PresentationDefinition {
        return content.presentationDefinition
    }
}
