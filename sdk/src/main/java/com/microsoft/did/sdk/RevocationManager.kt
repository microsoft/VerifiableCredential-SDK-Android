/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.RpDidToNameMap
import com.microsoft.did.sdk.datasource.repository.ReceiptRepository
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages revocation of Verifiable Presentation(s)
 */
@Singleton
class RevocationManager @Inject constructor(
    private val vchRepository: VerifiableCredentialHolderRepository,
    private val receiptRepository: ReceiptRepository
) {

    /**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential
     *
     * @param verifiableCredentialHolder The VC for which access to check status is revoked
     * @param rpDidToNameMap Map of DIDs and names of relying parties whose access is revoked
     * @param reason Reason for revocation
     */
    suspend fun revokeVerifiablePresentation(
        verifiableCredentialHolder: VerifiableCredentialHolder,
        rpDidToNameMap: RpDidToNameMap,
        reason: String = ""
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                vchRepository.revokeVerifiablePresentation(verifiableCredentialHolder, rpDidToNameMap.keys.toList(), reason).abortOnError()
                rpDidToNameMap.forEach { relyingParty ->
                    receiptRepository.createAndSaveReceiptsForVCs(
                        relyingParty.key,
                        relyingParty.value,
                        ReceiptAction.Revocation,
                        listOf(verifiableCredentialHolder.cardId)
                    )
                }
                Result.Success(Unit)
            }
        }
    }
}