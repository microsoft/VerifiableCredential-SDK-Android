/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.RpDidToNameMap
import com.microsoft.did.sdk.datasource.repository.ReceiptRepository
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
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
    private val vcRepository: VerifiableCredentialRepository,
    private val receiptRepository: ReceiptRepository,
    private val identifierManager: IdentifierManager
) {

    /**
     * Revokes a verifiable presentation which revokes access for relying parties listed to do a status check on the Verifiable Credential.
     * If relying party is not supplied, verifiable credential is revoked for all relying parties it has been presented.
     *
     * @param verifiableCredential The VC for which access to check status is revoked
     * @param rpDidToNameMap Map of DIDs and names of relying parties whose access is revoked. If empty, verifiable credential is revoked for all relying parties
     * @param reason Reason for revocation
     */
    suspend fun revokeSelectiveOrAllVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        rpDidToNameMap: RpDidToNameMap,
        reason: String = ""
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
                vcRepository.revokeVerifiablePresentation(verifiableCredential, masterIdentifier, rpDidToNameMap.keys.toList(), reason).abortOnError()
                rpDidToNameMap.forEach { relyingParty ->
                    receiptRepository.createAndSaveReceiptsForVCs(
                        relyingParty.key,
                        relyingParty.value,
                        ReceiptAction.Revocation,
                        listOf(verifiableCredential.jti)
                    )
                }
                Result.Success(Unit)
            }
        }
    }
}