/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.protectors.FormatterFactory
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.auth.validators.ValidatorFactory
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.AuthenticationException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IssuanceException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.PresentationException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import io.ktor.http.Url
import io.ktor.util.toMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val validatorFactory: ValidatorFactory,
    private val formatterFactory: FormatterFactory
) {

    /**
     * Get Presentation Request.
     *
     * @param uri OpenID Connect Uri that points to the presentation request.
     *
     * @return PresentationRequest object that contains all attestations.
     */
    suspend fun getPresentationRequest(uri: String): Result<PresentationRequest, Exception> {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            val protocolException = PresentationException("Request Protocol not supported.")
            return Result.Failure(protocolException)
        }

        val requestParameters = url.parameters.toMap()
        val serializedToken = requestParameters["request"]?.first()
        if (serializedToken != null) {
            return Result.Success(PresentationRequest(requestParameters, serializedToken))
        }

        val requestUri =
            requestParameters["request_uri"]?.first() ?: return Result.Failure(PresentationException("Cannot fetch request: No request uri found"))
        return when (val tokenResult = picRepository.getRequest(requestUri)) {
            is Result.Success -> Result.Success(PresentationRequest(requestParameters, tokenResult.payload))
            is Result.Failure -> Result.Failure(tokenResult.payload)
        }
    }

    /**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     *
     * @return IssuanceRequest object containing all metadata about what is needed to fulfill request including display information.
     */
    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest, Exception> {
        return when (val contractResult = picRepository.getContract(contractUrl)) {
            is Result.Success -> Result.Success(IssuanceRequest(contractResult.payload, contractUrl))
            is Result.Failure -> Result.Failure(contractResult.payload)
        }
    }

    /**
     * Validate an OpenID Connect Request.
     */
    suspend fun isValid(request: OidcRequest): Result<Boolean, Exception> {
        return try {
            val validator = validatorFactory.makeValidator(request)
            validator.validate(request)
        } catch (exception: Exception) {
            Result.Failure(exception)
        }
    }

    /**
     * Get contract Urls from VC Attestations.
     * Private Preview: gets first contract from each Verifiable Credential Attestation.
     */
    fun getContractUrls(request: OidcRequest): List<String> {
        val attestations = request.content.attestations ?: return emptyList()
        val contracts = mutableListOf<String>()
        attestations.presentations.forEach {
            contracts.add(it.contracts.first())
        }
        return contracts
    }

    /**
     * Create a Response from Request.
     */
    fun createResponse(request: Request): Result<Response, Exception> {
        return when (request) {
            is PresentationRequest -> Result.Success(PresentationResponse(request))
            is IssuanceRequest -> Result.Success(IssuanceResponse(request))
            else -> Result.Failure(AuthenticationException("Request Type not Supported."))
        }
    }

    /**
     * Send an Issuance Response.
     */
    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<IssuanceServiceResponse?, Exception> {
        val formatter = formatterFactory.makeFormatter(response)
        return when (val formattingResult = formatter.formAndSignResponse(response, responder)) {
            is Result.Success -> picRepository.sendIssuanceResponse(response.audience, formattingResult.payload)
            is Result.Failure -> {
                val exception = IssuanceException("Unable to format response", formattingResult.payload)
                Result.Failure(exception)
            }
        }
    }

    /**
     * Send a Presentation Response.
     */
    suspend fun sendPresentationResponse(response: PresentationResponse, responder: Identifier): Result<String, Exception> {
        val formatter = formatterFactory.makeFormatter(response)
        return when (val formattingResult = formatter.formAndSignResponse(response, responder)) {
            is Result.Success -> picRepository.sendPresentationResponse(response.audience, formattingResult.payload)
            is Result.Failure -> {
                val exception = PresentationException("Unable to format response.", formattingResult.payload)
                Result.Failure(exception)
            }
        }
    }

    /**
     * Puts together card and saves in repository.
     */
    suspend fun saveCard(signedVerifiableCredential: String, contract: PicContract): Result<PortableIdentityCard, Exception> {
        return try {
            val card = createCard(signedVerifiableCredential, contract)
            picRepository.insert(card)
            Result.Success(card)
        } catch (exception: Exception) {
            Result.Failure(exception)
        }
    }

    private fun createCard(signedVerifiableCredential: String, contract: PicContract): PortableIdentityCard {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        val verifiableCredential = VerifiableCredential(signedVerifiableCredential, contents)
        return PortableIdentityCard(contents.jti, verifiableCredential, contract.display)
    }

    private fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String): VerifiableCredentialContent {
        val token = JwsToken.deserialize(signedVerifiableCredential)
        return Serializer.parse(VerifiableCredentialContent.serializer(), token.content())
    }

    fun getCards(): Result<LiveData<List<PortableIdentityCard>>, Exception> {
        return try {
            Result.Success(picRepository.getAllCards())
        } catch (exception: Exception) {
            Result.Failure(exception)
        }
    }
}