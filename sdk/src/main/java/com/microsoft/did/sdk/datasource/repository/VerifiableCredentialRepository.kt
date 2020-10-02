/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.PresentationResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.RevocationResponseFormatter
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiablePresentationRevocationRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.DEFAULT_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.controlflow.ExchangeException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.RevocationException
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifiableCredentialRepository @Inject constructor(
    private val apiProvider: ApiProvider,
    private val issuanceResponseFormatter: IssuanceResponseFormatter,
    private val presentationResponseFormatter: PresentationResponseFormatter,
    private val exchangeResponseFormatter: ExchangeResponseFormatter,
    private val revocationResponseFormatter: RevocationResponseFormatter,
    private val serializer: Serializer
) {
    // Card Issuance Methods.
    suspend fun getContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        responder: Identifier,
        requestedVcMap: RequestedVcMap,
        expiryInSeconds: Int = DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<VerifiableCredential> {
        val formattedResponse = issuanceResponseFormatter.formatResponse(
            requestedVcMap = requestedVcMap,
            issuanceResponse = response,
            responder = responder,
            expiryInSeconds = expiryInSeconds
        )
        val rawVerifiableCredentialResult = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider
        ).fire()

        return when (rawVerifiableCredentialResult) {
            is Result.Success -> Result.Success(formVerifiableCredential(rawVerifiableCredentialResult.payload))
            is Result.Failure -> rawVerifiableCredentialResult
        }
    }

    // Presentation Methods.
    suspend fun getRequest(url: String) = FetchPresentationRequestNetworkOperation(url, apiProvider).fire()

    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        responder: Identifier,
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        expiryInSeconds: Int = DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<Unit> {
        val formattedResponse = presentationResponseFormatter.formatResponse(
            requestedVcPresentationSubmissionMap = requestedVcPresentationSubmissionMap,
            presentationResponse = response,
            responder = responder,
            expiryInSeconds = expiryInSeconds
        )
        return SendPresentationResponseNetworkOperation(
            response.audience,
            formattedResponse,
            response.request.content.state,
            apiProvider
        ).fire()
    }

    suspend fun revokeVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        owner: Identifier,
        rpList: List<String>,
        reason: String
    ): Result<RevocationReceipt> {
        val revocationRequest = RevocationRequest(verifiableCredential, owner, rpList, reason)
        val formattedRevocationRequest = revocationResponseFormatter.formatResponse(revocationRequest, DEFAULT_EXPIRATION_IN_SECONDS)
        return sendRevocationRequest(revocationRequest, formattedRevocationRequest)
    }

    suspend fun sendRevocationRequest(revocationRequest: RevocationRequest, formattedRevocationRequest: String): Result<RevocationReceipt> {
        val revocationResult = SendVerifiablePresentationRevocationRequestNetworkOperation(
            revocationRequest.audience,
            formattedRevocationRequest,
            apiProvider,
            serializer
        ).fire()
        return when (revocationResult) {
            is Result.Success -> revocationResult
            is Result.Failure -> Result.Failure(RevocationException("Unable to revoke VP"))
        }
    }

    suspend fun getExchangedVerifiableCredential(
        verifiableCredential: VerifiableCredential,
        masterIdentifier: Identifier,
        pairwiseIdentifier: Identifier
    ): Result<VerifiableCredential> {
        return sendExchangeRequest(ExchangeRequest(verifiableCredential, pairwiseIdentifier.id, masterIdentifier), DEFAULT_EXPIRATION_IN_SECONDS)
    }

    private suspend fun sendExchangeRequest(request: ExchangeRequest, expiryInSeconds: Int): Result<VerifiableCredential> {
        if (request.audience == "") {
            throw ExchangeException("Audience is an empty string.")
        }
        val formattedPairwiseRequest = exchangeResponseFormatter.formatResponse(request, expiryInSeconds)

        val result = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            request.audience,
            formattedPairwiseRequest,
            apiProvider
        ).fire()

        return when (result) {
            is Result.Success -> {
                val verifiableCredential = formVerifiableCredential(
                    result.payload,
                    request.verifiableCredential.picId
                )
                Result.Success(verifiableCredential)
            }
            is Result.Failure -> result
        }
    }

    private fun formVerifiableCredential(rawToken: String, vcId: String? = null): VerifiableCredential {
        val vcContents = unwrapSignedVerifiableCredential(rawToken, serializer)
        return VerifiableCredential(vcContents.jti, rawToken, vcContents, vcId ?: vcContents.jti)
    }
}