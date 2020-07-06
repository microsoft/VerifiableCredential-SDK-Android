/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.protectors.OidcResponseFormatter
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiableCredentialContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiablePresentationContext
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import com.microsoft.did.sdk.util.Constants.DEFAULT_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.controlflow.Result
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

    private val vcDao = database.verifiableCredentialDao()

    suspend fun insert(verifiableCredentialHolder: VerifiableCredentialHolder) = vchDao.insert(verifiableCredentialHolder)

    suspend fun delete(verifiableCredentialHolder: VerifiableCredentialHolder) = vchDao.delete(verifiableCredentialHolder)

    fun getAllVchs(): LiveData<List<VerifiableCredentialHolder>> = vchDao.getAllVcs()

    fun getVchsByType(type: String): LiveData<List<VerifiableCredentialHolder>> {
        return getAllVchs().map { cardList -> filterVcsByType(cardList, type) }
    }

    private fun filterVcsByType(vcList: List<VerifiableCredentialHolder>, type: String): List<VerifiableCredentialHolder> {
        return vcList.filter { it.verifiableCredential.contents.vc.type.contains(type) }
    }

    fun getVchById(id: String): LiveData<VerifiableCredentialHolder> = vchDao.getVcById(id)

    // Receipt Methods
    fun getAllReceiptsByVcId(vcId: String): LiveData<List<Receipt>> = receiptDao.getAllReceiptsByVcId(vcId)

    suspend fun insert(receipt: Receipt) = receiptDao.insert(receipt)

    // Verifiable Credential Methods
    private suspend fun getAllVerifiableCredentialsById(primaryVcId: String) =
        vcDao.getVerifiableCredentialById(primaryVcId)

    suspend fun insert(verifiableCredential: VerifiableCredential) = vcDao.insert(verifiableCredential)

    // Card Issuance Methods.
    suspend fun getContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendIssuanceResponse(response: IssuanceResponse,
                                     verifiableCredentialContexts: Map<String, VerifiableCredentialContext>?,
                                     responder: Identifier): Result<VerifiableCredential> {
        val formattedResponse = formatter.format(
            responder = responder,
            responseAudience = response.audience,
            presentationsAudience = response.request.entityIdentifier,
            verifiableCredentialContexts = verifiableCredentialContexts,
            idTokenContexts = response.getIdTokenContexts(),
            selfAttestedClaimContexts = response.getSelfAttestedClaimContexts(),
            contract = response.request.contractUrl,
            expiryInSeconds = DEFAULT_EXPIRATION_IN_SECONDS
        )
        val rawVerifiableCredentialResult = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider,
            serializer
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

    suspend fun sendPresentationResponse(response: PresentationResponse,
                                         verifiableCredentialContexts: Map<String, VerifiableCredentialContext>?,
                                         responder: Identifier,
                                         expiryInSeconds: Int): Result<Unit> {

        val state = response.request.content.state ?: ""
        val formattedResponse = formatter.format(
            responder = responder,
            responseAudience = response.audience,
            presentationsAudience = response.request.entityIdentifier,
            verifiableCredentialContexts = verifiableCredentialContexts,
            idTokenContexts = response.getIdTokenContexts(),
            selfAttestedClaimContexts = response.getSelfAttestedClaimContexts(),
            nonce = response.request.content.nonce,
            state = response.request.content.state,
            expiryInSeconds = expiryInSeconds
        )
        return SendPresentationResponseNetworkOperation(
            response.audience,
            formattedResponse,
            state,
            apiProvider
        ).fire()
    }

    suspend fun getExchangedVerifiableCredential(vpContext: VerifiablePresentationContext, newOwner: Identifier): Result<VerifiableCredential> {
        val verifiableCredentials = this.getAllVerifiableCredentialsById(vpContext.verifiablePresentationHolder.cardId)
        // if there is already a saved verifiable credential owned by pairwiseIdentifier return.
        verifiableCredentials.forEach {
            if (it.contents.sub == newOwner.id) {
                return Result.Success(it)
            }
        }
        val exchangeRequest =
            ExchangeRequest(
                vpContext.verifiablePresentationHolder.verifiableCredential,
                newOwner.id
            )
        return this.sendExchangeRequest(exchangeRequest, vpContext.verifiablePresentationHolder.owner)
    }

    private suspend fun sendExchangeRequest(request: ExchangeRequest, requester: Identifier): Result<VerifiableCredential> {
        val formattedPairwiseRequest = formatter.format(
            responder = requester,
            responseAudience = request.audience,
            transformingVerifiableCredential = request.verifiableCredential,
            recipientIdentifier = request.newOwnerDid,
            expiryInSeconds = DEFAULT_EXPIRATION_IN_SECONDS
        )

        val result = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            request.audience,
            formattedPairwiseRequest,
            apiProvider,
            serializer
        ).fire()

        return when (result) {
            is Result.Success -> {
                val verifiableCredential = formVerifiableCredential(
                    result.payload,
                    request.verifiableCredential.picId
                )
                this.insert(verifiableCredential)
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