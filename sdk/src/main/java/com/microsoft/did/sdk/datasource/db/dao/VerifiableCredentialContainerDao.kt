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
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContainer

@Dao
interface VerifiableCredentialContainerDao {

    @Query("SELECT * FROM VerifiableCredentialContainer")
    fun getAllCards(): LiveData<List<VerifiableCredentialContainer>>

    @Query("SELECT * FROM VerifiableCredentialContainer where picId = :id")
    fun getCardById(id: String): LiveData<VerifiableCredentialContainer>

    @Insert
    suspend fun insert(verifiableCredentialContainer: VerifiableCredentialContainer)

    @Delete
    suspend fun delete(verifiableCredentialContainer: VerifiableCredentialContainer)
}