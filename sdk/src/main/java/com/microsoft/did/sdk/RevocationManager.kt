// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.RevokedRPNameAndDid
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.createAndSaveReceiptsForVCs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RevocationManager (private val vchRepository: VerifiableCredentialHolderRepository) {

    /**
     * Revokes a verifiable presentation which revokes access for specific relying party/parties or all relying parties to do a status check on the Verifiable Credential
     *
     * @param verifiableCredentialHolder The VC for which access to check status is revoked
     * @param rpDidAndName Map of DIDs and names of relying parties whose access is revoked
     * @param reason Reason for revocation
     */

    suspend fun revokeVerifiablePresentation(
        verifiableCredentialHolder: VerifiableCredentialHolder,
        rpDidAndName: RevokedRPNameAndDid,
        reason: String = ""
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                vchRepository.revokeVerifiablePresentation(verifiableCredentialHolder, rpDidAndName.keys.toList(), reason).abortOnError()
                if (rpDidAndName.isEmpty())
                    createAndSaveReceiptsForVCs("", "", ReceiptAction.Revocation, listOf(verifiableCredentialHolder.cardId), vchRepository)
                else
                    rpDidAndName.forEach { relyingParty ->
                        createAndSaveReceiptsForVCs(
                            relyingParty.key,
                            relyingParty.value,
                            ReceiptAction.Revocation,
                            listOf(verifiableCredentialHolder.cardId),
                            vchRepository
                        )
                    }
                Result.Success(Unit)
            }
        }
    }
}