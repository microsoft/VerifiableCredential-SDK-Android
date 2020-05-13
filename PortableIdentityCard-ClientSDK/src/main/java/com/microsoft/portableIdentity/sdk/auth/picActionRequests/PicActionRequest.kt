/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.picActionRequests

import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.identifier.Identifier

/**
 * Sealed Class for Requests to the Portable Identity Card Service to do a certain action on a Verifiable Credential.
 */
sealed class PicActionRequest(val audience: String)

class PairwiseIssuanceRequest(val verifiableCredential: VerifiableCredential, val pairwiseIdentifier: String) : PicActionRequest(verifiableCredential.contents.vc.exchangeService?.id ?:"https://portableidentitycards.azure-api.net/v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/card/issue")
class RevocationRequest(val verifiableCredential: VerifiableCredential, val owner: Identifier) : PicActionRequest(verifiableCredential.contents.vc.revokeService?.id ?:"")
class StatusRequest(val verifiableCredential: VerifiableCredential, val owner: Identifier) : PicActionRequest(verifiableCredential.contents.vc.credentialStatus?.id ?:"")

