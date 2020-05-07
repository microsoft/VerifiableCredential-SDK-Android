package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential

@Dao
interface VerifiableCredentialDao {

    @Query("SELECT * FROM VerifiableCredential")
    fun getAllVerifiableCredentials(): LiveData<List<VerifiableCredential>>

    @Query("SELECT * FROM VerifiableCredential where primeId = :id")
    fun getVerifiableCredentialById(id:String): LiveData<List<VerifiableCredential>>

    @Insert
    suspend fun insert(verifiableCredential: VerifiableCredential)

    @Delete
    suspend fun delete(verifiableCredential: VerifiableCredential)
}