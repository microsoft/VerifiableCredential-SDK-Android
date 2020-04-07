/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.cards.deprecated.SerialClaimObject
import com.microsoft.portableIdentity.sdk.repository.networking.PicNetworkOperation
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources
 * that an app can have. In the common case there are two data sources: network and database. The repository decides
 * where to get this data, how and when to cache it, how to handle issues etc. so that the business logic will only
 * ever care to get the object it wants.
 */
@Singleton
class CardRepository @Inject constructor(database: SdkDatabase, private val picNetworkOperation: PicNetworkOperation) {

    private val cardDao = database.cardDao()

    private val serialClaimObjectDao = database.serialClaimObjectDao()

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    fun getAllClaimObjects(): LiveData<List<ClaimObject>> {
        val serialClaimObjects = serialClaimObjectDao.getAllClaimObjects()
        return Transformations.map(serialClaimObjects) { serialList -> transformList(serialList) }
    }

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    suspend fun insert(claimObject: ClaimObject) = serialClaimObjectDao.insert(SerialClaimObject(claimObject.serialize()))

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    suspend fun delete(claimObject: ClaimObject) = serialClaimObjectDao.delete(SerialClaimObject(claimObject.serialize()))

    @Deprecated("Old ClaimObject for old POC. Remove when new Model is up.")
    private fun transformList(serialClaimObjects: List<SerialClaimObject>): List<ClaimObject> =
        serialClaimObjects.map { Serializer.parse(ClaimObject.serializer(), it.serialClaimObject) }

    suspend fun insert(portableIdentityCard: PortableIdentityCard) = cardDao.insert(portableIdentityCard)

    suspend fun delete(portableIdentityCard: PortableIdentityCard) = cardDao.delete(portableIdentityCard)

    fun getAllCards(): LiveData<List<PortableIdentityCard>> {
        return cardDao.getAllCards()
    }

    suspend fun getContract(url: String) = picNetworkOperation.getContract(url)

    suspend fun getRequest(url: String) = picNetworkOperation.getRequest(url)

    suspend fun sendResponse(url: String, serializedResponse: String) = picNetworkOperation.sendResponse(url, serializedResponse)
}