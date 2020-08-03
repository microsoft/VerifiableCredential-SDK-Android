/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction

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