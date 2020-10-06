/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.util.controlflow.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages all functionality for managing, getting/creating and presenting Verifiable Credentials.
 * We only support OpenId Connect Protocol in order to get and present Verifiable Credentials.
 */
@Singleton
class VerifiableCredentialManager @Inject constructor(
    private val presentationService: PresentationService,
    private val issuanceService: IssuanceService,
    private val revocationService: RevocationService
) {

    /**
     * Load a Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     */
    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> = issuanceService.getIssuanceRequest(contractUrl)


    /**
     * Send an Issuance Response.
     *
     * @param response IssuanceResponse containing the requested attestations
     * @param enablePairwise when true a pairwise identifier will be used for this communication,
     * otherwise the master identifier is used which may allow the relying party to correlate the user
     */
    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        enablePairwise: Boolean = true
    ): Result<VerifiableCredential> = issuanceService.sendIssuanceResponse(response, enablePairwise)


    /**
     * Get Presentation Request.
     *
     * @param stringUri OpenID Connect Uri that points to the presentation request.
     */
    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> =
        presentationService.getPresentationRequest(stringUri)


    /**
     * Send a Presentation Response.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param enablePairwise when true a pairwise identifier will be used for this communication,
     * otherwise the master identifier is used which may allow the relying party to correlate the user
     */
    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        enablePairwise: Boolean = true
    ): Result<Unit> = presentationService.sendPresentationResponse(response, enablePairwise)


    /**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential.
     * If relying party is not supplied, verifiable credential is revoked for all relying parties it has been presented.
     *
     * @param verifiableCredential The VC for which access to check status is revoked
     * @param rpList DIDs of relying parties whose access is revoked. If empty, verifiable credential is revoked for all relying parties
     * @param reason Reason for revocation
     */
    suspend fun revokeSelectiveOrAllVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        rpList: List<String>,
        reason: String = ""
    ): Result<Unit> = revocationService.revokeSelectiveOrAllVerifiablePresentation(verifiableCredential, rpList, reason)
}

