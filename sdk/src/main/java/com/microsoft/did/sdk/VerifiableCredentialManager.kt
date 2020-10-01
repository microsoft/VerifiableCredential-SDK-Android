/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.net.Uri
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_HOST
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_SCHEME
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
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
    private val identifierManager: IdentifierManager,
    private val vcRepository: VerifiableCredentialRepository,
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
            return vcRepository.getRequest(requestUri)
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
            val contract = vcRepository.getContract(contractUrl).abortOnError()
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
                    response.requestedVcMap
                val verifiableCredential = vcRepository.sendIssuanceResponse(response, requestedVchMap).abortOnError()
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
                    response.requestedVcPresentationSubmissionMap
                vcRepository.sendPresentationResponse(response, vcRequestedMapping)
            }
        }
    }

    private suspend fun exchangeVcsInIssuanceRequest(response: IssuanceResponse): Result<RequestedVcMap> {
        return runResultTry {
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            val exchangedVcMap = response.requestedVcMap.mapValues {
                vcRepository.getExchangedVerifiableCredential(it.value, response.responder, masterIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcMap)
        }
    }

    private suspend fun exchangeVcsInPresentationRequest(response: PresentationResponse): Result<RequestedVcPresentationSubmissionMap> {
        return runResultTry {
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            val exchangedVcMap = response.requestedVcPresentationSubmissionMap.mapValues {
                vcRepository.getExchangedVerifiableCredential(it.value, response.responder, masterIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcPresentationSubmissionMap)
        }
    }
}
