// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.persistance.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.credentials.ClaimDescription
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.credentials.SerialClaimObject

@Dao
interface SerialClaimObjectDao {

    @Query("SELECT * FROM SerialClaimObject")
    fun getAllClaimObjects(): LiveData<List<SerialClaimObject>>

    @Insert
    suspend fun insert(serialClaimObject: SerialClaimObject)

    @Delete
    suspend fun delete(serialClaimObject: SerialClaimObject)
}