package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.identifier.LongformIdentifier

@Dao
interface PortableIdentityDao {

    @Insert
    fun insert(longformIdentifier: LongformIdentifier)

    @Query("SELECT * FROM LongformIdentifier where identifier = :identifier")
    fun queryIdentifier(identifier: String): LongformIdentifier
}