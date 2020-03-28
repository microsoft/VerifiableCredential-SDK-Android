package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.credentials.deprecated.SerialClaimObject

@Dao
interface SerialClaimObjectDao {

    @Query("SELECT * FROM SerialClaimObject")
    fun getAllClaimObjects(): LiveData<List<SerialClaimObject>>

    @Insert
    suspend fun insert(serialClaimObject: SerialClaimObject)

    @Delete
    suspend fun delete(serialClaimObject: SerialClaimObject)
}