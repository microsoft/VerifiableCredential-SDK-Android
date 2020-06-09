package com.microsoft.did.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.cards.receipts.Receipt

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM Receipt")
    fun getAllReceipts(): LiveData<List<Receipt>>

    @Query("SELECT * FROM Receipt WHERE cardId = :cardId")
    fun getAllReceiptsByCardId(cardId: String): LiveData<List<Receipt>>

    @Insert
    suspend fun insert(receipt: Receipt)
}