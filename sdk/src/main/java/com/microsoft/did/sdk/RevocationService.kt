// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.protectors.RevocationResponseFormatter
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiablePresentationRevocationRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.RevocationException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val identifierManager: IdentifierManager,
    private val revocationResponseFormatter: RevocationResponseFormatter,
    private val serializer: Serializer
) {

    /**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential.
     * If relying party is not supplied, verifiable credential is revoked for all relying parties it has been presented.
     *
     * @param verifiableCredential The VC for which access to check status is revoked
     * @param rpList DIDs of relying parties whose access is revoked. If empty, verifiable credential is revoked for all relying parties
     * @param reason Reason for revocation
     */
    suspend fun revokeSelectiveOrAllVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        rpList: List<String>,
        reason: String = ""
    ): Result<Unit> {
        return runResultTry {
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            revokeVerifiablePresentation(verifiableCredential, masterIdentifier, rpList, reason).abortOnError()
            Result.Success(Unit)
        }
    }

    private suspend fun revokeVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        owner: Identifier,
        rpList: List<String>,
        reason: String
    ): Result<RevocationReceipt> {
        val revocationRequest = RevocationRequest(verifiableCredential, owner, rpList, reason)
        val formattedRevocationRequest = revocationResponseFormatter.formatResponse(
            revocationRequest,
            Constants.DEFAULT_EXPIRATION_IN_SECONDS
        )
        return sendRevocationRequest(revocationRequest, formattedRevocationRequest)
    }

    private suspend fun sendRevocationRequest(
        revocationRequest: RevocationRequest,
        formattedRevocationRequest: String
    ): Result<RevocationReceipt> {
        val revocationResult = SendVerifiablePresentationRevocationRequestNetworkOperation(
            revocationRequest.audience,
            formattedRevocationRequest,
            apiProvider,
            serializer
        ).fire()
        return when (revocationResult) {
            is Result.Success -> revocationResult
            is Result.Failure -> Result.Failure(RevocationException("Unable to revoke VP"))
        }
    }
}