// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuanceManager @Inject constructor(
    private val identifierManager: IdentifierManager,
    private val vcRepository: VerifiableCredentialRepository
) {

    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val contract = vcRepository.getContract(contractUrl).abortOnError()
            val request = IssuanceRequest(contract, contractUrl)
            Result.Success(request)
        }
    }

    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        enablePairwise: Boolean = true
    ): Result<VerifiableCredential> {
        return runResultTry {
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            val verifiableCredential = if (enablePairwise) {
                val pairwiseIdentifier =
                    identifierManager.createPairwiseIdentifier(masterIdentifier, response.request.entityIdentifier).abortOnError()
                val requestedVcMap = exchangeVcsInIssuanceRequest(response, pairwiseIdentifier).abortOnError()
                vcRepository.sendIssuanceResponse(response, pairwiseIdentifier, requestedVcMap).abortOnError()
            } else {
                val requestedVcMap = response.requestedVcMap
                vcRepository.sendIssuanceResponse(response, masterIdentifier, requestedVcMap).abortOnError()
            }
            Result.Success(verifiableCredential)
        }
    }

    private suspend fun exchangeVcsInIssuanceRequest(
        response: IssuanceResponse,
        pairwiseIdentifier: Identifier
    ): Result<RequestedVcMap> {
        return runResultTry {
            val exchangedVcMap = response.requestedVcMap.mapValues {
                val owner = identifierManager.getIdentifierById(it.value.contents.sub).abortOnError()
                vcRepository.getExchangedVerifiableCredential(it.value, owner, pairwiseIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcMap)
        }
    }
}