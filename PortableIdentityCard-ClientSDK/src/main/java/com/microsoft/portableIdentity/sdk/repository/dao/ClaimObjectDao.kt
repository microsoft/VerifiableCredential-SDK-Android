package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.cards.deprecated.ClaimObject

@Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
@Dao
interface ClaimObjectDao {

    @Query("SELECT * FROM ClaimObject")
    fun getAllClaimObjects(): LiveData<List<ClaimObject>>

    @Insert
    suspend fun insert(claimObject: ClaimObject)

    @Delete
    suspend fun delete(claimObject: ClaimObject)
}