// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction

fun createReceipts(entityDid: String, entityName: String, receiptAction: ReceiptAction, vcIds: List<String>): List<Receipt> {
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