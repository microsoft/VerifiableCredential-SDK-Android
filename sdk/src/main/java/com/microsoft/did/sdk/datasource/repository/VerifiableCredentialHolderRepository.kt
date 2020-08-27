/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.protectors.OidcResponseFormatter
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.DEFAULT_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.controlflow.ExchangeException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources
 * that an app can have. In the common case there are two data sources: network and database. The repository decides
 * where to get this data, how and when to cache it, how to handle issues etc. so that the business logic will only
 * ever care to get the object it wants.
 */
@Singleton
class VerifiableCredentialHolderRepository @Inject constructor(
    database: SdkDatabase,
    private val apiProvider: ApiProvider,
    private val formatter: OidcResponseFormatter,
    private val serializer: Serializer
) {

    private val vchDao = database.verifiableCredentialHolderDao()

    private val receiptDao = database.receiptDao()

    suspend fun insert(verifiableCredentialHolder: VerifiableCredentialHolder) = vchDao.insert(verifiableCredentialHolder)

    suspend fun delete(verifiableCredentialHolder: VerifiableCredentialHolder) = vchDao.delete(verifiableCredentialHolder)

    fun getAllVchs(): LiveData<List<VerifiableCredentialHolder>> = vchDao.getAllVchs()

    fun queryAllVchs(): List<VerifiableCredentialHolder> = vchDao.queryAllVchs()

    fun getVchsByType(type: String): LiveData<List<VerifiableCredentialHolder>> {
        return getAllVchs().map { cardList -> filterVcsByType(cardList, type) }
    }

    private fun filterVcsByType(vcList: List<VerifiableCredentialHolder>, type: String): List<VerifiableCredentialHolder> {
        return vcList.filter { it.verifiableCredential.contents.vc.type.contains(type) }
    }

    fun getVchById(id: String): LiveData<VerifiableCredentialHolder> = vchDao.getVchById(id)

    // Receipt Methods
    fun getAllReceiptsByVcId(vcId: String): LiveData<List<Receipt>> = receiptDao.getAllReceiptsByVcId(vcId)

    suspend fun insert(receipt: Receipt) = receiptDao.insert(receipt)

    // Card Issuance Methods.
    suspend fun getContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        requestedVchMap: RequestedVchMap,
        responder: Identifier,
        expiryInSeconds: Int = DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<VerifiableCredential> {
        val formattedResponse = formatter.format(
            responder = responder,
            responseAudience = response.audience,
            presentationsAudience = response.request.entityIdentifier,
            requestedVchMap = requestedVchMap,
            requestedIdTokenMap = response.getRequestedIdTokens(),
            requestedSelfAttestedClaimMap = response.getRequestedSelfAttestedClaims(),
            contract = response.request.contractUrl,
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
    suspend fun getRequest(url: String) = FetchPresentationRequestNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        requestedVchMap: RequestedVchMap,
        responder: Identifier,
        expiryInSeconds: Int = DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<Unit> {

        val state = response.request.content.state
        val formattedResponse = formatter.format(
            responder = responder,
            responseAudience = response.audience,
            presentationsAudience = response.request.entityIdentifier,
            requestedVchMap = requestedVchMap,
            requestedIdTokenMap = response.getRequestedIdTokens(),
            requestedSelfAttestedClaimMap = response.getRequestedSelfAttestedClaims(),
            nonce = response.request.content.nonce,
            state = state,
            expiryInSeconds = expiryInSeconds
        )
        return SendPresentationResponseNetworkOperation(
            response.audience,
            formattedResponse,
            state,
            apiProvider
        ).fire()
    }

    suspend fun getExchangedVerifiableCredential(
        vch: VerifiableCredentialHolder,
        pairwiseIdentifier: Identifier
    ): Result<VerifiableCredential> {
        return sendExchangeRequest(ExchangeRequest(vch.verifiableCredential, pairwiseIdentifier.id), vch.owner)
    }

    private suspend fun sendExchangeRequest(request: ExchangeRequest, requester: Identifier): Result<VerifiableCredential> {
        if (request.audience == "") {
            throw ExchangeException("Audience is an empty string.")
        }
        val formattedPairwiseRequest = formatter.format(
            responder = requester,
            responseAudience = request.audience,
            transformingVerifiableCredential = request.verifiableCredential,
            recipientIdentifier = request.pairwiseDid,
            expiryInSeconds = DEFAULT_EXPIRATION_IN_SECONDS
        )

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