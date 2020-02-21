// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.persistance.repository

import androidx.lifecycle.LiveData
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.persistance.SdkDatabase

class VerifiableCredentialRepository(database: SdkDatabase) {

    private val claimObjectDao = database.claimObjectDao()

    fun getAllClaimObjects(): LiveData<List<ClaimObject>> = claimObjectDao.getAllClaimObjects()

    suspend fun insert(claimObject: ClaimObject) = claimObjectDao.insert(claimObject)

    suspend fun delete(claimObject: ClaimObject) = claimObjectDao.delete(claimObject)
}