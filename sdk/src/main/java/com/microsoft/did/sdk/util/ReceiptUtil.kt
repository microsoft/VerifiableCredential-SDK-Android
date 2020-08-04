/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.util.controlflow.RepositoryException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry

suspend fun createAndSaveReceiptsForVCs(
    entityDid: String,
    entityName: String,
    receiptAction: ReceiptAction,
    vcIds: List<String>,
    vchRepository: VerifiableCredentialHolderRepository
): Result<Unit> {
    return runResultTry {
        val receiptList = createReceiptsForVCs(entityDid, entityName, receiptAction, vcIds)
        receiptList.forEach { saveReceipt(it, vchRepository).abortOnError() }
        Result.Success(Unit)
    }
}

fun createReceiptsForVCs(entityDid: String, entityName: String, receiptAction: ReceiptAction, vcIds: List<String>): List<Receipt> {
    val receiptList = mutableListOf<Receipt>()
    vcIds.forEach {
        val receipt = createReceipt(entityDid, entityName, receiptAction, it)
        receiptList.add(receipt)
    }
    return receiptList
}

fun createReceipt(entityDid: String, entityName: String, receiptAction: ReceiptAction, vcId: String): Receipt {
    val date = System.currentTimeMillis()
    return Receipt(
        action = receiptAction,
        vcId = vcId,
        activityDate = date,
        entityIdentifier = entityDid,
        entityName = entityName
    )
}

/**
 * Get receipts by verifiable credential id from the database.
 */
private suspend fun saveReceipt(receipt: Receipt, vchRepository: VerifiableCredentialHolderRepository): Result<Unit> {
    return try {
        Result.Success(vchRepository.insert(receipt))
    } catch (exception: Exception) {
        Result.Failure(RepositoryException("Unable to insert receipt in repository.", exception))
    }
}