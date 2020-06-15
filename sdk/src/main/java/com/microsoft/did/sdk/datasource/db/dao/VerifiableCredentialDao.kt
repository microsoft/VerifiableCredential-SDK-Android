package com.microsoft.did.sdk.datasource.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.credential.models.VerifiableCredential

@Dao
interface VerifiableCredentialDao {

    @Query("SELECT * FROM VerifiableCredential where picId = :id")
    suspend fun getVerifiableCredentialById(id: String): List<VerifiableCredential>

    @Insert
    suspend fun insert(verifiableCredential: VerifiableCredential)

    @Delete
    suspend fun delete(verifiableCredential: VerifiableCredential)
}