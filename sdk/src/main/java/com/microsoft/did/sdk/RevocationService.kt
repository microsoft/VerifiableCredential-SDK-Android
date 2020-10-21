// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.protectors.RevocationResponseFormatter
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiablePresentationRevocationRequestNetworkOperation
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.RevocationException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val identifierManager: IdentifierManager,
    private val revocationResponseFormatter: RevocationResponseFormatter,
    private val serializer: Json
) {

    /**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential.
     *
     * @param verifiableCredential The VC for which access to check status is revoked
     * @param rpList DIDs of relying parties whose access is revoked.
     * @param reason Reason for revocation
     */
    suspend fun revokeVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        rpList: List<String>,
        reason: String = ""
    ): Result<RevocationReceipt> {
        return runResultTry {
            if (rpList.isEmpty()) throw RevocationException("No relying party has been provided.")
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            val revocationRequest = RevocationRequest(verifiableCredential, masterIdentifier, rpList, reason)
            val formattedRevocationRequest = revocationResponseFormatter.formatResponse(revocationRequest)
            sendRevocationRequest(revocationRequest, formattedRevocationRequest)
        }
    }

    private suspend fun sendRevocationRequest(
        revocationRequest: RevocationRequest,
        formattedRevocationRequest: String
    ): Result<RevocationReceipt> {
        return SendVerifiablePresentationRevocationRequestNetworkOperation(
            revocationRequest.audience,
            formattedRevocationRequest,
            apiProvider,
            serializer
        ).fire()
    }
}