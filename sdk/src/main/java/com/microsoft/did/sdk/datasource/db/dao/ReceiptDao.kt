package com.microsoft.did.sdk.datasource.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.credential.models.receipts.Receipt

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM Receipt")
    fun getAllReceipts(): LiveData<List<Receipt>>

    @Query("SELECT * FROM Receipt WHERE vcId = :cardId")
    fun getAllReceiptsByVcId(vcId: String): LiveData<List<Receipt>>

    @Insert
    suspend fun insert(receipt: Receipt)
}