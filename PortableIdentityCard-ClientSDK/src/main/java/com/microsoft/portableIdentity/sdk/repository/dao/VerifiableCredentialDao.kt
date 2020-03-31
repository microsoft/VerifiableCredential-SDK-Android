package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.credentials.VerifiableCredential

@Dao
interface VerifiableCredentialDao {

    @Query("SELECT * FROM ClaimObject")
    fun getAllVerifiableCredentials(): LiveData<List<VerifiableCredential>>

    @Insert
    suspend fun insert(verifiableCredential: VerifiableCredential)

    @Delete
    suspend fun delete(verifiableCredential: VerifiableCredential)
}