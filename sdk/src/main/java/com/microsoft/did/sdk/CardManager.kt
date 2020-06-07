/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.net.Uri
import androidx.lifecycle.LiveData
import com.microsoft.did.sdk.auth.models.contracts.PicContract
import com.microsoft.did.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.did.sdk.auth.requests.*
import com.microsoft.did.sdk.auth.responses.IssuanceResponse
import com.microsoft.did.sdk.auth.responses.PresentationResponse
import com.microsoft.did.sdk.auth.responses.Response
import com.microsoft.did.sdk.auth.validators.PresentationRequestValidator
import com.microsoft.did.sdk.cards.PortableIdentityCard
import com.microsoft.did.sdk.cards.receipts.Receipt
import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.repository.CardRepository
import com.microsoft.did.sdk.utilities.Constants.DEFAULT_EXPIRATION_IN_MINUTES
import com.microsoft.did.sdk.utilities.serializer.Serializer
import com.microsoft.did.sdk.utilities.controlflow.*
import com.microsoft.did.sdk.utilities.unwrapSignedVerifiableCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Verifiable Credentials.
 * We only support OpenId Connect Protocol in order to get and present Portable Identity Cards.
 */
@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator
) {

    /**
     * Get Presentation Request.
     *
     * @param stringUri OpenID Connect Uri that points to the presentation request.
     *
     * @return Result.Success: PresentationRequest object that contains all attestations.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val uri = verifyUri(stringUri)
                val requestToken = getPresentationRequestToken(uri).abortOnError()
                val tokenContents =
                    serializer.parse(OidcRequestContent.serializer(), JwsToken.deserialize(requestToken, serializer).content())
                val request = PresentationRequest(uri, requestToken, tokenContents)
                // TODO(Validate Presentation Requests)
                // isRequestValid(request).abortOnError()
                Result.Success(request)
            }
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (url.scheme != "openid") {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestToken(uri: Uri): Result<String> {
        return runResultTry {
            val serializedToken = uri.getQueryParameter("request")
            if (serializedToken != null) {
                Result.Success(serializedToken)
            }
            val requestUri = uri.getQueryParameter("request_uri")
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
     * @return Result.Success unit, if no validation exceptions were thrown.
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
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
     * @return Result.Success:
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<PortableIdentityCard> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val verifiableCredential = picRepository.sendIssuanceResponse(response, responder).abortOnError()
                picRepository.insert(verifiableCredential)
                val card = createCard(verifiableCredential.raw, responder, response.request.contract)
                createAndSaveReceipt(response)
                Result.Success(card)
            }
        }
    }

    /**
     * Send a Presentation Response signed by a responder Identifier.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param responder Identifier to be used to sign response.
     *
     * @return Result.Success: Unit
     *         Result.Failure: Exception explaining what went wrong.
     */
    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        responder: Identifier,
        expiresInMinutes: Int = DEFAULT_EXPIRATION_IN_MINUTES
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                picRepository.sendPresentationResponse(response, responder, expiresInMinutes)
                createAndSaveReceipt(response)
                Result.Success(Unit)
            }
        }
    }

    private suspend fun createAndSaveReceipt(response: Response) {
        val receipts = response.createReceiptsForPresentedCredentials(
            entityDid = response.request.entityIdentifier,
            entityName = response.request.entityName
        )
        receipts.forEach { saveReceipt(it) }
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
    suspend fun saveCard(portableIdentityCard: PortableIdentityCard): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                picRepository.insert(portableIdentityCard)
                Result.Success(Unit)
            }
        }
    }

    private fun createCard(signedVerifiableCredential: String, owner: Identifier, contract: PicContract): PortableIdentityCard {
        val contents =
            unwrapSignedVerifiableCredential(signedVerifiableCredential, serializer)
        val verifiableCredential = VerifiableCredential(contents.jti, signedVerifiableCredential, contents, contents.jti)
        return PortableIdentityCard(contents.jti, verifiableCredential, owner, contract.display)
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
