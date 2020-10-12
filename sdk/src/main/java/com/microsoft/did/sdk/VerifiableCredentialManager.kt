/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*//*


package com.microsoft.did.sdk

import android.net.Uri
import androidx.lifecycle.LiveData
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.RequestedVchPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.RpDidToNameMap
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.dnsBinding.DomainLinkageCredential
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.ReceiptRepository
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_HOST
import com.microsoft.did.sdk.util.Constants.DEEP_LINK_SCHEME
import com.microsoft.did.sdk.util.controlflow.DomainValidationException
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.ResolverException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.Success
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

*/
/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Verifiable Credentials.
 * We only support OpenId Connect Protocol in order to get and present Verifiable Credentials.
 *//*

@Singleton
class VerifiableCredentialManager @Inject constructor(
    private val vchRepository: VerifiableCredentialHolderRepository,
    private val receiptRepository: ReceiptRepository,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator,
    private val revocationManager: RevocationManager,
    private val resolver: Resolver
) {

    */
/**
     * Get Presentation Request.
     *
     * @param stringUri OpenID Connect Uri that points to the presentation request.
     *//*

    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val uri = verifyUri(stringUri)
                val requestToken = getPresentationRequestToken(uri).abortOnError()
                val tokenContents =
                    serializer.parse(
                        PresentationRequestContent.serializer(),
                        JwsToken.deserialize(requestToken, serializer).content()
                    )
                val didBoundToVerifierDid = getDomainForRp(tokenContents.issuer)
                val request = PresentationRequest(requestToken, tokenContents, "presentationtest.com")
                isRequestValid(request).abortOnError()
                Result.Success(request)
            }
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (url.scheme != DEEP_LINK_SCHEME && url.host != DEEP_LINK_HOST) {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestToken(uri: Uri): Result<String> {
        val serializedToken = uri.getQueryParameter("request")
        if (serializedToken != null) {
            return Result.Success(serializedToken)
        }
        val requestUri = uri.getQueryParameter("request_uri")
        if (requestUri != null) {
            return vchRepository.getRequest(requestUri)
        }
        return Result.Failure(PresentationException("No query parameter 'request' nor 'request_uri' is passed."))
    }

    */
/**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     *//*

    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val contract = vchRepository.getContract(contractUrl).abortOnError()
            val domainBoundToIssuerDid = getDomainForRp(contract.input.issuer)
            val request = IssuanceRequest(contract, contractUrl, "issuertest.com")
            Result.Success(request)
        }
    }

    */
/**
     * Validate an OpenID Connect Request with default Validator.
     *
     * @param request to be validated.
     *//*

    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
    }

    fun createIssuanceResponse(request: IssuanceRequest, responder: Identifier): IssuanceResponse {
        return IssuanceResponse(request, responder)
    }

    fun createPresentationResponse(request: PresentationRequest, responder: Identifier): PresentationResponse {
        return PresentationResponse(request, responder)
    }

    */
/**
     * Send an Issuance Response signed by a responder Identifier.
     *
     * @param response IssuanceResponse to be formed, signed, and sent.
     * @param exchangeForPairwiseVerifiableCredential Configuration to turn on/off pairwise exchange. It is set to true by default
     *//*

    suspend fun sendIssuanceResponse(
        response: IssuanceResponse,
        exchangeForPairwiseVerifiableCredential: Boolean = true
    ): Result<VerifiableCredentialHolder> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val requestedVchMap = if (exchangeForPairwiseVerifiableCredential)
                    exchangeVcsInIssuanceRequest(response).abortOnError()
                else
                    response.requestedVchMap
                val verifiableCredential = vchRepository.sendIssuanceResponse(response, requestedVchMap).abortOnError()
                val vch = createVch(verifiableCredential.raw, response.responder, response.request.contract)
                Result.Success(vch)
            }
        }
    }

    */
/**
     * Send a Presentation Response signed by a responder Identifier.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param exchangeForPairwiseVerifiableCredential Configuration to turn on/off pairwise exchange. It is set to true by default
     *//*

    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        exchangeForPairwiseVerifiableCredential: Boolean = true
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val vcRequestedMapping = if (exchangeForPairwiseVerifiableCredential)
                    exchangeVcsInPresentationRequest(response).abortOnError()
                else
                    response.requestedVchPresentationSubmissionMap
                vchRepository.sendPresentationResponse(response, vcRequestedMapping).abortOnError()
                receiptRepository.createAndSaveReceiptsForVCs(
                    response.request.entityIdentifier,
                    response.request.entityName,
                    ReceiptAction.Presentation,
                    vcRequestedMapping.values.map { it.cardId }
                )
                Result.Success(Unit)
            }
        }
    }

    */
/**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential.
     * If relying party is not supplied, verifiable credential is revoked for all relying parties it has been presented.
     *
     * @param verifiableCredentialHolder The VC for which access to check status is revoked
     * @param rpDidToNameMap Map of DIDs and names of relying parties whose access is revoked. If empty, verifiable credential is revoked for all relying parties
     * @param reason Reason for revocation
     *//*

    suspend fun revokeSelectiveOrAllVerifiablePresentation(
        verifiableCredentialHolder: VerifiableCredentialHolder,
        rpDidToNameMap: RpDidToNameMap,
        reason: String = ""
    ): Result<Unit> {
        return revocationManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, rpDidToNameMap, reason)
    }

    private suspend fun exchangeVcsInIssuanceRequest(response: IssuanceResponse): Result<RequestedVchMap> {
        return runResultTry {
            val responder = response.responder
            val verifiableCredentialHolderRequestMappings = response.requestedVchMap
            val exchangedVcMap = verifiableCredentialHolderRequestMappings.mapValues {
                VerifiableCredentialHolder(
                    it.value.cardId,
                    vchRepository.getExchangedVerifiableCredential(it.value, responder).abortOnError(),
                    it.value.owner,
                    it.value.displayContract
                )
            }
            Result.Success(exchangedVcMap as RequestedVchMap)
        }
    }

    private suspend fun exchangeVcsInPresentationRequest(response: PresentationResponse): Result<RequestedVchPresentationSubmissionMap> {
        return runResultTry {
            val responder = response.responder
            val verifiableCredentialHolderRequestMappings = response.requestedVchPresentationSubmissionMap
            val exchangedVcMap = verifiableCredentialHolderRequestMappings.mapValues {
                VerifiableCredentialHolder(
                    it.value.cardId,
                    vchRepository.getExchangedVerifiableCredential(it.value, responder).abortOnError(),
                    it.value.owner,
                    it.value.displayContract
                )
            }
            Result.Success(exchangedVcMap as RequestedVchPresentationSubmissionMap)
        }
    }



    */
/**
     * Saves a Verifiable Credential Holder to the database
     *//*

    suspend fun saveVch(verifiableCredentialHolder: VerifiableCredentialHolder): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                vchRepository.insert(verifiableCredentialHolder)
                Result.Success(Unit)
            }
        }
    }

    private fun createVch(
        signedVerifiableCredential: String,
        owner: Identifier,
        contract: VerifiableCredentialContract
    ): VerifiableCredentialHolder {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential, serializer)
        val verifiableCredential = VerifiableCredential(contents.jti, signedVerifiableCredential, contents, contents.jti)
        return VerifiableCredentialHolder(
            contents.jti,
            verifiableCredential,
            owner,
            contract.display
        )
    }

    */
/**
     * Get all Verifiable Credentials Holders from the database.
     *//*

    fun getAllActiveVerifiableCredentials(): LiveData<List<VerifiableCredentialHolder>> {
        return vchRepository.getAllActiveVchs()
    }

    fun queryAllActiveVerifiableCredentials(): List<VerifiableCredentialHolder> {
        return vchRepository.queryAllActiveVchs()
    }

    fun getArchivedVerifiableCredentials(): LiveData<List<VerifiableCredentialHolder>> {
        return vchRepository.getArchivedVchs()
    }

    */
/**
     * Get all Verifiable Credentials Holders from the database by credential type.
     *//*

    fun getVchsByType(type: String): LiveData<List<VerifiableCredentialHolder>> {
        return vchRepository.getVchsByType(type)
    }

    */
/**
     * Get all Verifiable Credentials Holders from the database by credential type.
     *//*

    fun queryVchsByType(type: String): List<VerifiableCredentialHolder> {
        return vchRepository.queryVchsByType(type)
    }

    */
/**
     * Get receipts by verifiable credential id from the database.
     *//*

    fun getReceiptByVcId(vcId: String): LiveData<List<Receipt>> {
        return receiptRepository.getAllReceiptsByVcId(vcId)
    }

    */
/**
     * Get receipts by verifiable credential id from the database.
     *//*

    private fun queryReceiptByVcId(vcId: String): List<Receipt> {
        return receiptRepository.queryAllReceiptsByVcId(vcId)
    }

    */
/**
     * Get a Verifiable Credential by id from the database.
     *//*

    fun getVchById(id: String): LiveData<VerifiableCredentialHolder> {
        return vchRepository.getVchById(id)
    }

    suspend fun setIsArchived(vch: VerifiableCredentialHolder, isArchived: Boolean): Result<VerifiableCredentialHolder> {
        val updatedVch = VerifiableCredentialHolder(vch.cardId, vch.verifiableCredential, vch.owner, vch.displayContract, isArchived)
        withContext(Dispatchers.IO) {
            vchRepository.update(updatedVch)
        }
        return Result.Success(updatedVch)
    }

    suspend fun deleteVch(vch: VerifiableCredentialHolder): Result<Unit> {
        vchRepository.delete(vch)
        return Result.Success(Unit)
    }

    */
/**
     * Retrieves RPs to whom VC has been presented
     * @param vcId id of VC for which RPs presented to is retrieved
     *//*

    fun getRpsFromPresentationsOfVc(vcId: String): RpDidToNameMap {
        val receiptsOfVc = queryReceiptByVcId(vcId)
        val receiptsForPresentations = receiptsOfVc.filter { it.action == ReceiptAction.Presentation }
        return receiptsForPresentations.map { it.entityIdentifier to it.entityName }.toMap()
    }
}
*/
