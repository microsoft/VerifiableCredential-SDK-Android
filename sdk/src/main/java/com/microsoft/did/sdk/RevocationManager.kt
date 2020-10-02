/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
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


    suspend fun revokeSelectiveOrAllVerifiablePresentation(
        verifiableCredential: VerifiableCredential,
        rpList: List<String>,
        reason: String = ""
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runResultTry {
                val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
                vcRepository.revokeVerifiablePresentation(verifiableCredential, masterIdentifier, rpList, reason).abortOnError()
                Result.Success(Unit)
            }
        }
    }
}