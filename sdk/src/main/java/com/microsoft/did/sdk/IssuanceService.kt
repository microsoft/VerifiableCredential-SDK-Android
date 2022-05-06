/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendIssuanceCompletionResponse
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.internal.ImageLoader
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.ExpiredTokenException
import com.microsoft.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.did.sdk.util.controlflow.InvalidPinException
import com.microsoft.did.sdk.util.controlflow.NotFoundException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.logTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuanceService @Inject constructor(
    private val identifierService: IdentifierService,
    private val linkedDomainsService: LinkedDomainsService,
    private val apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val issuanceResponseFormatter: IssuanceResponseFormatter,
    private val serializer: Json,
    private val imageLoader: ImageLoader
) {

    /**
     * Load a Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     */
    suspend fun getRequest(
        contractUrl: String
    ): Result<IssuanceRequest> {
        return runResultTry {
            logTime("Issuance getRequest") {
                val contract = fetchContract(contractUrl).abortOnError()
                val linkedDomainResult = linkedDomainsService.fetchAndVerifyLinkedDomains(contract.input.issuer).abortOnError()
                val request = IssuanceRequest(contract, contractUrl, linkedDomainResult)
                imageLoader.loadRemoteImage(request)
                Result.Success(request)
            }
        }
    }

    private suspend fun fetchContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider,
        jwtValidator,
        serializer
    ).fire()

    /**
     * Send an Issuance Response.
     *
     * @param response IssuanceResponse containing the requested attestations
     */
    suspend fun sendResponse(
        response: IssuanceResponse
    ): Result<VerifiableCredential> {
        return runResultTry {
            logTime("Issuance sendResponse") {
                val masterIdentifier = identifierService.getMasterIdentifier().abortOnError()
                val requestedVcMap = response.requestedVcMap
                val verifiableCredential = formAndSendResponse(response, masterIdentifier, requestedVcMap).abortOnError()
                Result.Success(verifiableCredential)
            }
        }
    }

    suspend fun sendCompletionResponse(completionResponse: IssuanceCompletionResponse, url: String): Result<Unit> {
        return runResultTry {
            logTime("Issuance sendCompletionResponse") {
                SendIssuanceCompletionResponse(
                    url,
                    serializer.encodeToString(completionResponse),
                    apiProvider
                ).fire()
            }
        }
    }

    private suspend fun formAndSendResponse(
        response: IssuanceResponse,
        responder: Identifier,
        requestedVcMap: RequestedVcMap,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<VerifiableCredential> {
        val formattedResponse = issuanceResponseFormatter.formatResponse(
            requestedVcMap = requestedVcMap,
            issuanceResponse = response,
            responder = responder,
            expiryInSeconds = expiryInSeconds
        )
        val result = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider,
            jwtValidator,
            serializer
        ).fire()
        when (result) {
            is Result.Failure -> {
                when (val sdkException = result.payload) {
                    is ForbiddenException -> {
                        val exception = InvalidPinException("Incorrect verification code", false)
                        exception.apply {
                            correlationVector = sdkException.correlationVector
                            errorBody = sdkException.errorBody
                            errorCode = sdkException.errorCode
                            innerErrorCodes = sdkException.innerErrorCodes
                            errorMessage = sdkException.errorMessage
                        }
                        return Result.Failure(exception)
                    }
                }
            }
            is Result.Success -> return result
        }
        return result
    }
}