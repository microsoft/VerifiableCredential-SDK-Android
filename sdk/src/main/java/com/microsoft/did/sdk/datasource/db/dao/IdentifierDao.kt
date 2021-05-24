package com.microsoft.did.sdk.datasource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microsoft.did.sdk.identifier.models.Identifier

@Dao
interface IdentifierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(identifier: Identifier)

    @Query("SELECT * FROM Identifier where id = :identifier")
    suspend fun queryByIdentifier(identifier: String): Identifier?

    //TODO: See how identifiers are stored with pairwise in picture and modify accordingly
    @Query("SELECT * FROM Identifier where name= :name")
    suspend fun queryByName(name: String): Identifier?

    @Query("SELECT * FROM Identifier")
    suspend fun queryAllLocal(): List<Identifier>

    @Query("DELETE FROM Identifier where id = :identifier")
    suspend fun deleteIdentifier(identifier: String)

    @Query("DELETE FROM Identifier")
    suspend fun deleteAll()
}