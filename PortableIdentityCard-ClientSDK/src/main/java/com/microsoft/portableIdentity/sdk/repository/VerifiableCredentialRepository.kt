package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.credentials.deprecated.SerialClaimObject
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import javax.inject.Inject

class VerifiableCredentialRepository @Inject constructor(database: SdkDatabase) {

    private val claimObjectDao = database.claimObjectDao()

    private val serialClaimObjectDao = database.serialClaimObjectDao()

    fun getAllClaimObjects(): LiveData<List<ClaimObject>> {
        val serialClaimObjects = serialClaimObjectDao.getAllClaimObjects()
        return Transformations.map(serialClaimObjects) { serialList -> transformList(serialList) }
    }

    suspend fun insert(claimObject: ClaimObject) = serialClaimObjectDao.insert(SerialClaimObject(claimObject.serialize()))

    suspend fun delete(claimObject: ClaimObject) = serialClaimObjectDao.delete(SerialClaimObject(claimObject.serialize()))

    private fun transformList(serialClaimObjects: List<SerialClaimObject>): List<ClaimObject> =
        serialClaimObjects.map { Serializer.parse(ClaimObject.serializer(), it.serialClaimObject) }
}