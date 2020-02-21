// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.persistance.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.credentials.ClaimObject

@Dao
interface ClaimObjectDao {

    @Query("SELECT * FROM ClaimObject")
    fun getAllClaimObjects(): LiveData<List<ClaimObject>>

    @Insert
    suspend fun insert(claimObject: ClaimObject)

    @Delete
    suspend fun delete(claimObject: ClaimObject)
}