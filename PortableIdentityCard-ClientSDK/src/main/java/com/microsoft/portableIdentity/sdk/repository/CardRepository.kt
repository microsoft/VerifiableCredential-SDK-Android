/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PairwiseIssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import com.microsoft.portableIdentity.sdk.repository.networking.cardOperations.FetchContractNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.cardOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.cardOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.cardOperations.SendPresentationResponseNetworkOperation
import com.microsoft.portableIdentity.sdk.utilities.Constants.DEFAULT_EXPIRATION_IN_MINUTES
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.PairwiseIssuanceException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources
 * that an app can have. In the common case there are two data sources: network and database. The repository decides
 * where to get this data, how and when to cache it, how to handle issues etc. so that the business logic will only
 * ever care to get the object it wants.
 */
@Singleton
class CardRepository @Inject constructor(
    database: SdkDatabase,
    private val apiProvider: ApiProvider,
    private val formatter: OidcResponseFormatter,
    private val serializer: Serializer
) {

    private val cardDao = database.cardDao()

    private val receiptDao = database.receiptDao()

    private val verifiableCredentialDao = database.verifiableCredentialDao()

    // Portable Identity Card Methods
    suspend fun insert(portableIdentityCard: PortableIdentityCard) = cardDao.insert(portableIdentityCard)

    suspend fun delete(portableIdentityCard: PortableIdentityCard) = cardDao.delete(portableIdentityCard)

    fun getAllCards(): LiveData<List<PortableIdentityCard>> = cardDao.getAllCards()

    fun getCardsByType(type: String): LiveData<List<PortableIdentityCard>> {
        return getAllCards().map { cardList -> filterCardsByType(cardList, type) }
    }

    private fun filterCardsByType(cardList: List<PortableIdentityCard>, type: String): List<PortableIdentityCard> {
        return cardList.filter { it.verifiableCredential.contents.vc.type.contains(type) }
    }

    fun getCardById(id: String): LiveData<PortableIdentityCard> = cardDao.getCardById(id)

    // Receipt Methods
    fun getAllReceipts(): LiveData<List<Receipt>> = receiptDao.getAllReceipts()

    fun getAllReceiptsByCardId(cardId: String): LiveData<List<Receipt>> = receiptDao.getAllReceiptsByCardId(cardId)

    suspend fun insert(receipt: Receipt) = receiptDao.insert(receipt)

    // Verifiable Credential Methods
    fun getAllVerifiableCredentials(): List<VerifiableCredential> = verifiableCredentialDao.getAllVerifiableCredentials()

    private fun getAllVerifiableCredentialsByPrimaryVcId(primaryVcId: String): List<VerifiableCredential> =
        verifiableCredentialDao.getVerifiableCredentialByPrimaryVcId(primaryVcId)

    suspend fun insert(verifiableCredential: VerifiableCredential) = verifiableCredentialDao.insert(verifiableCredential)

    // Issuance Methods.
    suspend fun getContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<VerifiableCredential>  {
        val formattedResponse = formatter.format(
            responder = responder,
            audience = response.audience,
            requestedVcs = response.getCollectedCards().mapValues { getPairwiseVerifiableCredentialFromCard(it.value, responder) },
            requestedIdTokens = response.getCollectedIdTokens(),
            requestedSelfIssuedClaims = response.getCollectedIdTokens(),
            contract = response.request.contractUrl,
            expiresIn = DEFAULT_EXPIRATION_IN_MINUTES
        )
        return SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider,
            serializer
        ).fire()
    }

    // Presentation Methods.
    suspend fun getRequest(url: String) = FetchPresentationRequestNetworkOperation(
        url,
        apiProvider
    ).fire()

    suspend fun sendPresentationResponse(response: PresentationResponse, responder: Identifier): Result<Unit> {
        val formattedResponse = formatter.format(
            responder = responder,
            audience = response.audience,
            requestedVcs = response.getCollectedCards().mapValues { it.value.verifiableCredential }, //getPairwiseVerifiableCredentialFromCard(it.value, responder) },
            requestedIdTokens = response.getCollectedIdTokens(),
            requestedSelfIssuedClaims = response.getCollectedIdTokens(),
            nonce = response.request.content.nonce,
            state = response.request.content.state,
            expiresIn = DEFAULT_EXPIRATION_IN_MINUTES
        )
        return SendPresentationResponseNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider
        ).fire()
    }

    private suspend fun getPairwiseVerifiableCredentialFromCard(card: PortableIdentityCard, responder: Identifier): VerifiableCredential {
        return getPairwiseVerifiableCredential(card.verifiableCredential, responder, card.owner)
    }

    private suspend fun getPairwiseVerifiableCredential(
        verifiableCredential: VerifiableCredential,
        pairwiseIdentifier: Identifier,
        primaryIdentifier: Identifier
    ): VerifiableCredential {
        val verifiableCredentials = this.getAllVerifiableCredentialsByPrimaryVcId(verifiableCredential.primaryVcId)
        // if there is already a verifiable credential owned by pairwiseIdentifier return.
        verifiableCredentials.forEach {
            if (it.contents.sub == pairwiseIdentifier.id) {
                return it
            }
        }
        val pairwiseRequest = PairwiseIssuanceRequest(verifiableCredential)
        val formattedPairwiseRequest = formatter.format(
            responder = primaryIdentifier,
            audience = pairwiseRequest.audience,
            transformingVerifiableCredential = verifiableCredential,
            recipientIdentifier = pairwiseIdentifier.id,
            expiresIn = DEFAULT_EXPIRATION_IN_MINUTES,
            requestedSelfIssuedClaims = emptyMap(),
            requestedIdTokens = emptyMap(),
            requestedVcs = emptyMap()
        )
        val pairwiseVerifiableCredential = this.sendPairwiseIssuanceRequest(pairwiseRequest.audience, formattedPairwiseRequest)
        this.insert(pairwiseVerifiableCredential)
        return pairwiseVerifiableCredential
    }

    private suspend fun sendPairwiseIssuanceRequest(audience: String, requestToken: String): VerifiableCredential {
        val pairwiseVerifiableCredentialResult = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            audience,
            requestToken,
            apiProvider,
            serializer
        ).fire()
        // we can't return a result here, so need to unwrap
        return when (pairwiseVerifiableCredentialResult) {
            is Result.Success -> pairwiseVerifiableCredentialResult.payload
            is Result.Failure -> throw PairwiseIssuanceException("Unable to reissue Pairwise Verifiable Credential.", pairwiseVerifiableCredentialResult.payload)
        }
    }
}