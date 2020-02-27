// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.persistance.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.credentials.SerialClaimObject
import com.microsoft.did.sdk.persistance.SdkDatabase
import com.microsoft.did.sdk.utilities.Serializer

class VerifiableCredentialRepository(database: SdkDatabase) {

    private val claimObjectDao = database.claimObjectDao()

    private val serialClaimObjectDao = database.serialClaimObjectDao()

    fun getAllClaimObjects(): LiveData<List<ClaimObject>> {
        val serialClaimObjects = serialClaimObjectDao.getAllClaimObjects()
        return Transformations.map(serialClaimObjects, { serialList -> transformList(serialList) })
    }

    suspend fun insert(claimObject: ClaimObject) = serialClaimObjectDao.insert(SerialClaimObject(claimObject.serialize()))

    suspend fun delete(claimObject: ClaimObject) = serialClaimObjectDao.delete(SerialClaimObject(claimObject.serialize()))

    private fun transformList(serialClaimObjects: List<SerialClaimObject>): List<ClaimObject> =
        serialClaimObjects.map { Serializer.parse(ClaimObject.serializer(), it.serialClaimObject) }

    private fun <T, K, R> LiveData<T>.combineWith(
        liveData: LiveData<K>,
        block: (T?, K?) -> R
    ): LiveData<R> {
        val result = MediatorLiveData<R>()
        result.addSource(this) {
            result.value = block.invoke(this.value, liveData.value)
        }
        result.addSource(liveData) {
            result.value = block.invoke(this.value, liveData.value)
        }
        return result
    }
}

