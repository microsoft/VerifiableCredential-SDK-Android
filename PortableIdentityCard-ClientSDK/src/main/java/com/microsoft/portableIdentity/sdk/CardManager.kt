/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.portableIdentity.sdk.auth.protectors.Formatter
import com.microsoft.portableIdentity.sdk.auth.requests.*
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.auth.validators.Validator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.*
import io.ktor.http.Url
import io.ktor.util.toMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Portable Identity Cards.
 * We only support OpenId Connect Protocol in order to get and present Portable Identity Cards.
 */
@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val serializer: Serializer,
    private val validator: Validator,
    private val formatter: Formatter,
    private val cardRequestBindingCreator: CardRequestBindingCreator
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
            if (request.hasPresentationAttestations()) {
                val bindings = getCardRequestBinding(request.getPresentationAttestations())
                request.addPresentationBindings(bindings)
            }
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

    private fun getCardRequestBinding(presentationAttestations: List<PresentationAttestation>): CardRequestBinding {
        val savedCards = getCards().value ?: throw RepositoryException("Cards not founds.")
        return cardRequestBindingCreator.getRequiredSavedCards(presentationAttestations, savedCards)
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
            if (request.hasPresentationAttestations()) {
                val bindings = getCardRequestBinding(request.getPresentationAttestations())
                request.addPresentationBindings(bindings)
            }
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
    suspend fun isValid(request: OidcRequest): Result<Boolean> {
        return validator.validate(request)
    }

    /**
     * Create a Response from Request.
     *
     * @param request to create response for (could be an Issuance or Presentation request)
     *
     * @return Result.Success: Response that was created.
     *         Result.Failure: Exception because request type not supported.
     */
    fun createResponse(request: Request): Result<Response> {
        return when (request) {
            is PresentationRequest -> Result.Success(PresentationResponse(request))
            is IssuanceRequest -> Result.Success(IssuanceResponse(request))
            else -> Result.Failure(AuthenticationException("Request Type not Supported."))
        }
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
    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<PortableIdentityCard> {
        return runResultTry {
            val formattedResponse = formatter.formAndSignResponse(response, responder).abortOnError()
            val verifiableCredential = picRepository.sendIssuanceResponse(response.audience, formattedResponse).abortOnError()
            Result.Success(createCard(verifiableCredential.raw, response.request.contract))
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
    suspend fun sendPresentationResponse(response: PresentationResponse, responder: Identifier): Result<PresentationServiceResponse> {
        return runResultTry {
            val formattedResponse = formatter.formAndSignResponse(response, responder).abortOnError()
            picRepository.sendPresentationResponse(response.audience, formattedResponse)
        }
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
    suspend fun saveCard(signedVerifiableCredential: String, response: IssuanceResponse): Result<PortableIdentityCard> {
        return try {
            val card = createCard(signedVerifiableCredential, response.request.contract)
            picRepository.insert(card)
            Result.Success(card)
        } catch (exception: Exception) {
            Result.Failure(RepositoryException("Unable to insert card in repository.", exception))
        }
    }

    fun createCard(signedVerifiableCredential: String, contract: PicContract): PortableIdentityCard {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        val verifiableCredential = VerifiableCredential(signedVerifiableCredential, contents)
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
}