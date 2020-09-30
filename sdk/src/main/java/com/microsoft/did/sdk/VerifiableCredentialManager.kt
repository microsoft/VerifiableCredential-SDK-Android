/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.net.Uri
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.entities.receipts.Receipt
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.RequestedVchPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_HOST
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_SCHEME
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Verifiable Credentials.
 * We only support OpenId Connect Protocol in order to get and present Verifiable Credentials.
 */
@Singleton
class VerifiableCredentialManager @Inject constructor(
    private val vchRepository: VerifiableCredentialRepository,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator
) {

    /**
     * Get Presentation Request.
     *
     * @param stringUri OpenID Connect Uri that points to the presentation request.
     */
    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val uri = verifyUri(stringUri)
                val requestToken = getPresentationRequestToken(uri).abortOnError()
                val tokenContents =
                    serializer.parse(
                        PresentationRequestContent.serializer(),
                        JwsToken.deserialize(requestToken, serializer).content()
                    )
                val request = PresentationRequest(requestToken, tokenContents)
                isRequestValid(request).abortOnError()
                Result.Success(request)
            }
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (url.scheme != DEEP_LINK_SCHEME && url.host != DEEP_LINK_HOST) {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestToken(uri: Uri): Result<String> {
        val serializedToken = uri.getQueryParameter("request")
        if (serializedToken != null) {
            return Result.Success(serializedToken)
        }
        val requestUri = uri.getQueryParameter("request_uri")
        if (requestUri != null) {
            return vchRepository.getRequest(requestUri)
        }
        return Result.Failure(PresentationException("No query parameter 'request' nor 'request_uri' is passed."))
    }

    /**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     */
    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val contract = vchRepository.getContract(contractUrl).abortOnError()
            val request = IssuanceRequest(contract, contractUrl)
            Result.Success(request)
        }
    }

    /**
     * Validate an OpenID Connect Request with default Validator.
     *
     * @param request to be validated.
     */
    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
    }

    /**
     * Send an Issuance Response signed by a responder Identifier.
     *
     * @param response IssuanceResponse to be formed, signed, and sent.
     * @param exchangeForPairwiseVerifiableCredential Configuration to turn on/off pairwise exchange. It is set to true by default
     */
    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        exchangeForPairwiseVerifiableCredential: Boolean = true
    ): Result<VerifiableCredential> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val requestedVchMap = if (exchangeForPairwiseVerifiableCredential)
                    exchangeVcsInIssuanceRequest(response).abortOnError()
                else
                    response.requestedVchMap
                val verifiableCredential = vchRepository.sendIssuanceResponse(response, requestedVchMap).abortOnError()
                Result.Success(verifiableCredential)
            }
        }
    }

    /**
     * Send a Presentation Response signed by a responder Identifier.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param exchangeForPairwiseVerifiableCredential Configuration to turn on/off pairwise exchange. It is set to true by default
     */
    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        exchangeForPairwiseVerifiableCredential: Boolean = true
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val vcRequestedMapping = if (exchangeForPairwiseVerifiableCredential)
                    exchangeVcsInPresentationRequest(response).abortOnError()
                else
                    response.requestedVchPresentationSubmissionMap
                vchRepository.sendPresentationResponse(response, vcRequestedMapping).abortOnError()
                createAndSaveReceipt(response).abortOnError()
                Result.Success(Unit)
            }
        }
    }

    private suspend fun exchangeVcsInIssuanceRequest(response: IssuanceResponse): Result<RequestedVchMap> {
        return runResultTry {
            val responder = response.responder
            val verifiableCredentialHolderRequestMappings = response.requestedVchMap
            val exchangedVcMap = verifiableCredentialHolderRequestMappings.mapValues {
                VerifiableCredentialHolder(
                    it.value.cardId,
                    vchRepository.getExchangedVerifiableCredential(it.value, responder).abortOnError(),
                    it.value.owner,
                    it.value.displayContract
                )
            }
            Result.Success(exchangedVcMap as RequestedVchMap)
        }
    }

    private suspend fun exchangeVcsInPresentationRequest(response: PresentationResponse): Result<RequestedVchPresentationSubmissionMap> {
        return runResultTry {
            val responder = response.responder
            val verifiableCredentialHolderRequestMappings = response.requestedVchPresentationSubmissionMap
            val exchangedVcMap = verifiableCredentialHolderRequestMappings.mapValues {
                VerifiableCredentialHolder(
                    it.value.cardId,
                    vchRepository.getExchangedVerifiableCredential(it.value, responder).abortOnError(),
                    it.value.owner,
                    it.value.displayContract
                )
            }
            Result.Success(exchangedVcMap as RequestedVchPresentationSubmissionMap)
        }
    }

    private suspend fun createAndSaveReceipt(response: PresentationResponse): Result<Unit> {
        return runResultTry {
            val receipts = response.createReceiptsForPresentedVerifiableCredentials(
                entityDid = response.request.entityIdentifier,
                entityName = response.request.entityName
            )
            receipts.forEach { saveReceipt(it).abortOnError() }
            Result.Success(Unit)
        }
    }
}
