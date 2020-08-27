/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder

@Dao
interface VerifiableCredentialHolderDao {

    @Query("SELECT * FROM VerifiableCredentialHolder")
    fun getAllVchs(): LiveData<List<VerifiableCredentialHolder>>

    @Query("SELECT * FROM VerifiableCredentialHolder")
    fun queryAllVchs(): List<VerifiableCredentialHolder>

    @Query("SELECT * FROM VerifiableCredentialHolder where picId = :id")
    fun getVchById(id: String): LiveData<VerifiableCredentialHolder>

    @Insert
    suspend fun insert(verifiableCredentialHolder: VerifiableCredentialHolder)

    @Delete
    suspend fun delete(verifiableCredentialHolder: VerifiableCredentialHolder)
}