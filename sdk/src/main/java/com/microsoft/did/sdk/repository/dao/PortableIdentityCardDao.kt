/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.cards.PortableIdentityCard

@Dao
interface PortableIdentityCardDao {

    @Query("SELECT * FROM PortableIdentityCard")
    fun getAllCards(): LiveData<List<PortableIdentityCard>>

    @Query("SELECT * FROM PortableIdentityCard where picId = :id")
    fun getCardById(id: String): LiveData<PortableIdentityCard>

    @Insert
    suspend fun insert(portableIdentityCard: PortableIdentityCard)

    @Delete
    suspend fun delete(portableIdentityCard: PortableIdentityCard)
}