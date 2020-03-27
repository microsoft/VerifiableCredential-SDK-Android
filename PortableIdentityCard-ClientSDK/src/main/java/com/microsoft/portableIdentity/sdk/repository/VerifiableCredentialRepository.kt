package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.credentials.deprecated.SerialClaimObject
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import javax.inject.Inject

/**
 * Repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources
 * that an app can have. In the common case there are two data sources: network and database. The repository decides
 * where to get this data, how and when to cache it, how to handle issues etc. so that the business logic will only
 * ever care to get the object it wants.
 */
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