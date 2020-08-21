/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContentForPresentation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import kotlinx.serialization.Serializable

@Serializable
sealed class Request(val entityName: String = "", val entityIdentifier: String = "")

@Serializable
class IssuanceRequest(val contract: VerifiableCredentialContract, val contractUrl: String, val attestations: CredentialAttestations) :
    Request(contract.display.card.issuedBy, contract.input.issuer)

@Serializable
class PresentationRequest(
    val serializedToken: String,
    val content: OidcRequestContentForPresentation,
    val presentationDefinition: PresentationDefinition
) : Request(content.registration.clientName, content.iss)