/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.protectors.Formatter
import com.microsoft.portableIdentity.sdk.auth.requests.*
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.validators.Validator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Constants.DEFAULT_EXPIRATION_IN_MINUTES
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.*
import io.ktor.http.Url
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Portable Identity Cards.
 * We only support OpenId Connect Protocol in order to get and present Portable Identity Cards.
 */
@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val serializer: Serializer,
    private val validator: Validator,
    private val formatter: Formatter
) {

    /**
     * Get Presentation Request.
     *
     * @param uri OpenID Connect Uri that points to the presentation request.
     *
     * @return Result.Success: PresentationRequest object that contains all attestations.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun getPresentationRequest(uri: String): Result<PresentationRequest> {
        return runResultTry {
            val url = verifyUri(uri)
            val requestParameters = url.parameters.toMap()
            val requestToken = getPresentationRequestToken(requestParameters).abortOnError()
            val tokenContents = serializer.parse(OidcRequestContent.serializer(), JwsToken.deserialize(requestToken, serializer).content())
            val request = PresentationRequest(requestParameters, requestToken, tokenContents)
            Result.Success(request)
        }
    }

    private fun verifyUri(uri: String): Url {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestToken(requestParameters: Map<String, List<String>>): Result<String> {
        return runResultTry {
            val serializedToken = requestParameters["request"]?.first()
            if (serializedToken != null) {
                Result.Success(serializedToken)
            }
            val requestUri = requestParameters["request_uri"]?.first()
            if (requestUri == null) {
                Result.Failure(PresentationException("Request Uri does not exist."))
            } else {
                picRepository.getRequest(requestUri)
            }
        }
    }

    /**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     *
     * @return Result.Success: IssuanceRequest object containing all metadata about what is needed to fulfill request including display information.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val contract = picRepository.getContract(contractUrl).abortOnError()
            val request = IssuanceRequest(contract, contractUrl)
            Result.Success(request)
        }
    }

    /**
     * Validate an OpenID Connect Request with default Validator.
     *
     * @param request to be validated.
     *
     * @return Result.Success true, if request is valid, false if it is not valid.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun isValid(request: PresentationRequest): Result<Boolean> {
        return validator.validate(request)
    }

    fun createIssuanceResponse(request: IssuanceRequest): IssuanceResponse {
        return IssuanceResponse(request)
    }

    fun createPresentationResponse(request: PresentationRequest): PresentationResponse {
        return PresentationResponse(request)
    }

    /**
     * Send an Issuance Response signed by a responder Identifier.
     *
     * @param response IssuanceResponse to be formed, signed, and sent.
     * @param responder Identifier to be used to sign response.
     *
     * @return Result.Success: TODO("Support Error cases better (ex. 404)").
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<Pair<PortableIdentityCard, List<Receipt>>> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                response.transformCollectedCards { runBlocking { getPairwiseCard(it, responder) } }
                val formattedResponse = formatter.formAndSignRequest(response, responder, DEFAULT_EXPIRATION_IN_MINUTES).abortOnError()
                SdkLog.i(formattedResponse)
                val verifiableCredential = picRepository.sendIssuanceResponse(response.audience, formattedResponse).abortOnError()
                val card = createCard(verifiableCredential.raw, response.request.contract)
                val receipts = response.createReceiptsForPresentedCredentials(
                    entityDid = response.request.contract.input.issuer,
                    entityHostName = response.audience,
                    entityName = response.request.entityName,
                    requestToken = formattedResponse
                ).toMutableList()
                receipts.add(
                    response.createReceipt(
                        ReceiptAction.Issuance,
                        card.cardId,
                        card.verifiableCredential.contents.iss,
                        response.audience,
                        response.request.entityName,
                        formattedResponse
                    )
                )
                receipts.forEach { saveReceipt(it).abortOnError() }
                Result.Success(Pair(card, receipts))
            }
        }
    }

    /**
     * Send a Presentation Response signed by a responder Identifier.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param responder Identifier to be used to sign response.
     *
     * @return Result.Success: TODO("Support Error cases better (ex. 404)").
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun sendPresentationResponse(response: PresentationResponse, responder: Identifier, expiresInMinutes: Int = DEFAULT_EXPIRATION_IN_MINUTES): Result<List<Receipt>> {
        return runResultTry {
            response.transformCollectedCards { runBlocking { getPairwiseCard(it, responder) } }
            val formattedResponse = formatter.formAndSignRequest(response, responder, expiresInMinutes).abortOnError()
            picRepository.sendPresentationResponse(response.audience, formattedResponse)
            val receipts = response.createReceiptsForPresentedCredentials(
                entityDid = response.request.content.iss,
                entityHostName = response.request.entityIdentifier,
                entityName = response.request.entityName,
                requestToken = formattedResponse
            )
            receipts.forEach { saveReceipt(it).abortOnError() }
            Result.Success(receipts)
        }
    }

    /**
     * Change Owner of Portable Identity Card.
     * Card id and display information will be the same as the origin card.
     * Only change will be the Pairwise Identifier who the card belongs to.
     */
    private suspend fun getPairwiseCard(card: PortableIdentityCard, responder: Identifier): PortableIdentityCard {
        val newVerifiableCredential = getPairwiseVerifiableCredential(card.cardId, responder.id)
        return PortableIdentityCard(card.cardId, newVerifiableCredential, card.displayContract)
    }

    private suspend fun getPairwiseVerifiableCredential(primaryVcId: String, pairwiseIdentifier: String): VerifiableCredential {
        val verifiableCredentials = picRepository.getAllVerifiableCredentialsByPrimaryVcId(primaryVcId)
        // if there is already a verifiable credential owned by pairwiseIdentifier return.
        verifiableCredentials.forEach {
            if (it.contents.sub == pairwiseIdentifier) {
                return it
            }
        }

        // else get a new Verifiable Credential from Issuance Service
        TODO("make network call to get new Verifiable Credential")
    }

    /**
     * Puts together a Portable Identity Card and saves in repository.
     *
     * @param signedVerifiableCredential in Compact JWT form.
     * @param response that was used to get the verifiable credential.
     *
     * @return Result.Success: Portable Identity Card that was saved to Storage.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun saveCard(portableIdentityCard: PortableIdentityCard): Result<Nothing?> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                picRepository.insert(portableIdentityCard)
                Result.Success(null)
            }
        }
    }

    private fun createCard(signedVerifiableCredential: String, contract: PicContract): PortableIdentityCard {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        val verifiableCredential = VerifiableCredential(contents.jti, signedVerifiableCredential, contents, contents.jti)
        return PortableIdentityCard(contents.jti, verifiableCredential, contract.display)
    }

    private fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String): VerifiableCredentialContent {
        val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
        return serializer.parse(VerifiableCredentialContent.serializer(), token.content())
    }

    /**
     * Get All Portable Identity Cards from Storage.
     *
     * @return Result.Success: List of Portable Identity Card from Storage.
     *         Result.Failure: Exception explaining what went wrong.
     */
    fun getCards(): LiveData<List<PortableIdentityCard>> {
        return picRepository.getAllCards()
    }

    /**
     * Get All Portable Identity Cards from Storage by Credential Type.
     *
     * @return Result.Success: Filtered List of Portable Identity Card from Storage.
     *         Result.Failure: Exception explaining what went wrong.
     */
    fun getCardsByType(type: String): LiveData<List<PortableIdentityCard>> {
        return picRepository.getCardsByType(type)
    }

    /**
     * Get Receipts by Card Id from Storage.
     *
     * @return List of Receipts
     */
    fun getReceiptByCardId(cardId: String): LiveData<List<Receipt>> {
        return picRepository.getAllReceiptsByCardId(cardId)
    }

    /**
     * Get Receipts by Card Id from Storage.
     *
     * @return List of Receipts
     */
    private suspend fun saveReceipt(receipt: Receipt): Result<Unit> {
        return try {
            Result.Success(picRepository.insert(receipt))
        } catch (exception: Exception) {
            Result.Failure(RepositoryException("Unable to insert receipt in repository.", exception))
        }
    }

     /** Get A Portable Identity Card by card id from storage
     * @param  id: card id of requested card
     * @return Result.Success: Portable Identity Card corresponding to id passed
     *         Result.Failure: Exception explaining the problem
     */
    fun getCardById(id: String): LiveData<PortableIdentityCard> {
        return picRepository.getCardById(id)
    }
}
