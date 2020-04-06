/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.AuthenticationException
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.ServiceResponse
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseSigner
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import io.ktor.http.Url
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val cryptoOperations: CryptoOperations,
    private val resolver: IResolver,
    private val validator: OidcRequestValidator, // TODO: should this be a generic Validator?
    private val signer: OidcResponseSigner,
    private val formatter: OidcResponseFormatter
) {

    /**
     * Create a Request Object from a uri.
     */
    suspend fun getRequest(uri: String): OidcRequest {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            throw AuthenticationException("request format not supported")
        }

        val requestParameters = url.parameters.toMap()
        val serializedToken = requestParameters["request"]?.first()
        if (serializedToken != null) {
            return OidcRequest(requestParameters, serializedToken)
        }

        val requestUri = requestParameters["request_uri"]?.first() ?: throw AuthenticationException("Cannot fetch request: No request uri found")
        val requestToken = picRepository.getRequest(requestUri) ?: throw AuthenticationException("Cannot fetch request: No request token found")
        return OidcRequest(requestParameters, requestToken)
    }

    /**
     * Validate an OpenID Connect Request.
     */
    suspend fun isValid(request: OidcRequest): Boolean {
        return validator.validate(request)
    }

    /**
     * Get contract from PICS.
     * PP: gets first contract from each Verifiable Credential Attestation.
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
     * Get contract from PICS.
     */
    suspend fun getContract(url: String): PicContract? {
        return picRepository.getContract(url)
    }

    /**
     * Create OidcResponse from OidcRequest.
     */
    fun createResponse(request: OidcRequest): OidcResponse {
        return PresentationResponse(request)
    }
    fun createResponse(contract: PicContract): OidcResponse {
        return IssuanceResponse(contract)
    }

    /**
     * Send a Response.
     */
    suspend fun sendResponse(response: OidcResponse, responderIdentifier: Identifier): ServiceResponse {
        val responseContent = formatter.formContents(response, responderIdentifier.document.id, responderIdentifier.signatureKeyReference)
        val serializedResponseContent = Serializer.stringify(OidcResponseContent.serializer(), responseContent)
        val signedResponse = signer.sign(serializedResponseContent, responderIdentifier.signatureKeyReference)
        val serializedSignedResponse = signedResponse.serialize()
        return picRepository.sendResponse(response.audience, serializedSignedResponse) ?: throw AuthenticationException("Unable to send response.")
    }

    /**
     * Puts together card and saves in repository.
     */
    suspend fun saveCard(signedVerifiableCredential: String, contract: PicContract) {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        val verifiableCredential = VerifiableCredential(signedVerifiableCredential, contents)
        val card = PortableIdentityCard(contents.jti, verifiableCredential, contract.display)
        picRepository.insert(card)
    }

    fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String): VerifiableCredentialContent {
        val token = JwsToken.deserialize(signedVerifiableCredential)
        return Serializer.parse(VerifiableCredentialContent.serializer(), token.content())
    }

    fun getCards(): LiveData<List<PortableIdentityCard>> {
        return picRepository.getAllCards()
    }

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    suspend fun saveClaim(claim: ClaimObject) {
        picRepository.insert(claim)
    }

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    fun getClaims(): LiveData<List<ClaimObject>> {
        return picRepository.getAllClaimObjects()
    }

    @Deprecated("Old OidcRequest for old POC. Remove when new Model is up.")
    suspend fun parseOidcRequest(request: String): com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcRequest {
        return withContext(Dispatchers.IO) {
            com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcRequest.parseAndVerify(request, cryptoOperations, resolver)
        }
    }
}