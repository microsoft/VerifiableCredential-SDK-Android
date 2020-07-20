/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

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
import com.microsoft.did.sdk.credential.service.Response
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContent
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.DEFAULT_EXPIRATION_IN_MINUTES
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.RepositoryException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages all functionality for managing, getting/creating, presenting, and storing Verifiable Credentials.
 * We only support OpenId Connect Protocol in order to get and present Verifiable Credentials.
 */
@Singleton
class VerifiableCredentialManager @Inject constructor(
    private val vchRepository: VerifiableCredentialHolderRepository,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator
) {

    /**
     * Get Presentation Request.
     *
     * @param stringUri OpenID Connect Uri that points to the presentation request.
     */
    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val uri = verifyUri(stringUri)
                val requestToken = getPresentationRequestToken(uri).abortOnError()
                val tokenContents =
                    serializer.parse(OidcRequestContent.serializer(), JwsToken.deserialize(requestToken, serializer).content())
                val request = PresentationRequest(uri, requestToken, tokenContents)
                isRequestValid(request).abortOnError()
                Result.Success(request)
            }
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (url.scheme != Constants.DEEP_LINK_SCHEME) {
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

    /**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     */
    suspend fun getIssuanceRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val contract = vchRepository.getContract(contractUrl).abortOnError()
            val request = IssuanceRequest(contract, contractUrl)
            Result.Success(request)
        }
    }

    /**
     * Validate an OpenID Connect Request with default Validator.
     *
     * @param request to be validated.
     */
    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
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
     */
    suspend fun sendIssuanceResponse(response: IssuanceResponse, responder: Identifier): Result<VerifiableCredentialHolder> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val verifiableCredential = vchRepository.sendIssuanceResponse(response, responder).abortOnError()
                vchRepository.insert(verifiableCredential)
                val vch = createVch(verifiableCredential.raw, responder, response.request.contract)
                createAndSaveReceipt(response)
                Result.Success(vch)
            }
        }
    }

    /**
     * Send a Presentation Response signed by a responder Identifier.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param responder Identifier to be used to sign response.
     */
    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        responder: Identifier,
        expiresInMinutes: Int = DEFAULT_EXPIRATION_IN_MINUTES
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                vchRepository.sendPresentationResponse(response, responder, expiresInMinutes).abortOnError()
                createAndSaveReceipt(response).abortOnError()
                Result.Success(Unit)
            }
        }
    }

    suspend fun revokeVerifiablePresentation(
        verifiableCredentialHolder: VerifiableCredentialHolder,
        rpList: Map<String, String>?,
        reason: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val status = vchRepository.revokeVerifiablePresentation(verifiableCredentialHolder, rpList?.values?.toList(), reason).abortOnError()
                createAndSaveRevocationReceipt(
                    status.rp?.joinToString(",") ?: "",
                    rpList?.keys?.toList()?.joinToString(",") ?: "",
                    ReceiptAction.Revocation,
                    verifiableCredentialHolder.cardId
                )
                Result.Success(Unit)
            }
        }
    }

    private suspend fun createAndSaveRevocationReceipt(entityDid: String, entityName: String, receiptAction: ReceiptAction, vcId: String): Result<Unit> {
        return runResultTry {
            val receipt = createReceiptsForRevokedCredentials(entityDid, entityName, receiptAction, vcId)
            saveReceipt(receipt).abortOnError()
            Result.Success(Unit)
        }
    }

    fun createReceiptsForRevokedCredentials(entityDid: String, entityName: String, receiptAction: ReceiptAction, vcId: String): Receipt {
        val date = System.currentTimeMillis()
        return Receipt(
            action = receiptAction,
            vcId = vcId,
            activityDate = date,
            entityIdentifier = entityDid,
            entityName = entityName
        )
    }

    private suspend fun createAndSaveReceipt(response: Response): Result<Unit> {
        return runResultTry {
            val receipts = response.createReceiptsForPresentedCredentials(
                entityDid = response.request.entityIdentifier,
                entityName = response.request.entityName
            )
            receipts.forEach { saveReceipt(it).abortOnError() }
            Result.Success(Unit)
        }
    }

    /**
     * Saves a Verifiable Credential Holder to the database
     */
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
        val contents =
            unwrapSignedVerifiableCredential(signedVerifiableCredential, serializer)
        val verifiableCredential = VerifiableCredential(contents.jti, signedVerifiableCredential, contents, contents.jti)
        return VerifiableCredentialHolder(
            contents.jti,
            verifiableCredential,
            owner,
            contract.display
        )
    }

    /**
     * Get all Verifiable Credentials Holders from the database.
     */
    fun getVerifiableCredentials(): LiveData<List<VerifiableCredentialHolder>> {
        return vchRepository.getAllVchs()
    }

    /**
     * Get all Verifiable Credentials Holders from the database by credential type.
     */
    fun getVchsByType(type: String): LiveData<List<VerifiableCredentialHolder>> {
        return vchRepository.getVchsByType(type)
    }

    /**
     * Get receipts by verifiable credential id from the database.
     */
    fun getReceiptByVcId(vcId: String): LiveData<List<Receipt>> {
        return vchRepository.getAllReceiptsByVcId(vcId)
    }

    /**
     * Get receipts by verifiable credential id from the database.
     */
    private suspend fun saveReceipt(receipt: Receipt): Result<Unit> {
        return try {
            Result.Success(vchRepository.insert(receipt))
        } catch (exception: Exception) {
            Result.Failure(RepositoryException("Unable to insert receipt in repository.", exception))
        }
    }

    /**
     * Get a Verifiable Credential by id from the database.
     */
    fun getVchById(id: String): LiveData<VerifiableCredentialHolder> {
        return vchRepository.getVchById(id)
    }
}
