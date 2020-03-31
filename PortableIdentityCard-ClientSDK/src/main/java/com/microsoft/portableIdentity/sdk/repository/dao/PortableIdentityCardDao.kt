package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.cards.Card
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialDescriptor

@Dao
interface PortableIdentityCardDao {

    @Query("SELECT * FROM Card")
    fun getAllCards(): LiveData<List<Card>>

    @Insert
    suspend fun insert(card: Card)

    @Delete
    suspend fun delete(card: Card)
}