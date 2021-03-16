/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.internal.ImageLoader
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.logTime
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuanceService @Inject constructor(
    private val identifierManager: IdentifierManager,
    private val exchangeService: ExchangeService,
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
    suspend fun getRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            logTime("Issuance getRequest") {
                val contract = fetchContract(contractUrl).abortOnError()
                val linkedDomainResult = linkedDomainsService.fetchAndVerifyLinkedDomains(contract.input.issuer).abortOnError()
                val request = IssuanceRequest(contract, contractUrl, linkedDomainResult)
                imageLoader.loadRemoteImagesIntoContract(request)
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
     * @param enablePairwise when true a pairwise identifier will be used for this communication,
     * otherwise the master identifier is used which may allow the relying party to correlate the user
     */
    suspend fun sendResponse(
        response: IssuanceResponse,
        enablePairwise: Boolean = true
    ): Result<VerifiableCredential> {
        return runResultTry {
            logTime("Issuance sendResponse") {
                val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
                val verifiableCredential = if (enablePairwise) {
                    val pairwiseIdentifier =
                        identifierManager.getOrCreatePairwiseIdentifier(masterIdentifier, response.request.entityIdentifier).abortOnError()
                    val requestedVcMap = exchangeVcsInIssuanceRequest(response, pairwiseIdentifier).abortOnError()
                    formAndSendResponse(response, pairwiseIdentifier, requestedVcMap).abortOnError()
                } else {
                    val requestedVcMap = response.requestedVcMap
                    formAndSendResponse(response, masterIdentifier, requestedVcMap).abortOnError()
                }
                Result.Success(verifiableCredential)
            }
        }
    }

    private suspend fun exchangeVcsInIssuanceRequest(
        response: IssuanceResponse,
        pairwiseIdentifier: Identifier
    ): Result<RequestedVcMap> {
        return runResultTry {
            val exchangedVcMap = response.requestedVcMap.mapValues {
                val owner = identifierManager.getIdentifierById(it.value.contents.sub).abortOnError()
                exchangeService.getExchangedVerifiableCredential(it.value, owner, pairwiseIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcMap)
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
        return SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider,
            jwtValidator,
            serializer
        ).fire()
    }
}