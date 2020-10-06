// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.ExchangeException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.formVerifiableCredential
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val exchangeResponseFormatter: ExchangeResponseFormatter,
    private val serializer: Serializer
) {

    suspend fun getExchangedVerifiableCredential(
        verifiableCredential: VerifiableCredential,
        owner: Identifier,
        pairwiseIdentifier: Identifier
    ): Result<VerifiableCredential> {
        return sendExchangeRequest(
            ExchangeRequest(verifiableCredential, pairwiseIdentifier.id, owner),
            Constants.DEFAULT_EXPIRATION_IN_SECONDS
        )
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
                    request.verifiableCredential.picId,
                    serializer
                )
                Result.Success(verifiableCredential)
            }
            is Result.Failure -> result
        }
    }
}