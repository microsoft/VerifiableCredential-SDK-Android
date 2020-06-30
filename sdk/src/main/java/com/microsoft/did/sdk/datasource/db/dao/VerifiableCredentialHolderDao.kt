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
import androidx.room.Update
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder

@Dao
interface VerifiableCredentialHolderDao {

    @Query("SELECT * FROM VerifiableCredentialHolder")
    fun getAllVcs(): LiveData<List<VerifiableCredentialHolder>>

    @Query("SELECT * FROM VerifiableCredentialHolder where picId = :id")
    fun getVcById(id: String): LiveData<VerifiableCredentialHolder>

    @Insert
    suspend fun insert(verifiableCredentialHolder: VerifiableCredentialHolder)

    @Delete
    suspend fun delete(verifiableCredentialHolder: VerifiableCredentialHolder)

    @Query("UPDATE VerifiableCredentialHolder SET credentialStatus = :status")
    suspend fun update(verifiableCredentialHolder: VerifiableCredentialHolder, status: String)
}