package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.identifier.Identifier

@Dao
interface PortableIdentityDao {

    @Insert
    fun insert(identifier: Identifier)

    @Query("SELECT * FROM Identifier where identifier = :identifier")
    fun queryById(identifier: String): Identifier

    //TODO: See how identifiers are stored with pairwise in picture and modify accordingly
    @Query("SELECT * FROM Identifier where name= :name")
    fun queryByName(name: String): Identifier
}