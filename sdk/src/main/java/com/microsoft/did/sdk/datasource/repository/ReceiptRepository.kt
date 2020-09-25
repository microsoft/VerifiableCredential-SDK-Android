/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import androidx.lifecycle.LiveData
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepository @Inject constructor(database: SdkDatabase) {

    private val receiptDao = database.receiptDao()

    suspend fun insert(receipt: Receipt) = receiptDao.insert(receipt)

    fun getAllReceiptsByVcId(vcId: String): LiveData<List<Receipt>> = receiptDao.getAllReceiptsByVcId(vcId)

    fun getAllReceipts(): LiveData<List<Receipt>> = receiptDao.getAllReceipts()

    suspend fun createAndSaveReceiptsForVCs(
        entityDid: String,
        entityName: String,
        receiptAction: ReceiptAction,
        vcIds: List<String>
    ) {
        val receiptList = createReceiptsForVCs(entityDid, entityName, receiptAction, vcIds)
        receiptList.forEach { insert(it) }
    }

    private fun createReceiptsForVCs(
        entityDid: String,
        entityName: String,
        receiptAction: ReceiptAction,
        vcIds: List<String>
    ): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        vcIds.forEach {
            val receipt = Receipt(receiptAction, entityDid, entityName, it)
            receiptList.add(receipt)
        }
        return receiptList
    }
}