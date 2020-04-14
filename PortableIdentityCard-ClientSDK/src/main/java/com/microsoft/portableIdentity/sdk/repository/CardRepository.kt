/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.repository.networking.HttpResult
import com.microsoft.portableIdentity.sdk.repository.networking.IssuanceNetworkOperations
import com.microsoft.portableIdentity.sdk.repository.networking.PresentationNetworkOperations
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IssuanceException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

/**
 * Repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources
 * that an app can have. In the common case there are two data sources: network and database. The repository decides
 * where to get this data, how and when to cache it, how to handle issues etc. so that the business logic will only
 * ever care to get the object it wants.
 */
@Singleton
class CardRepository @Inject constructor(database: SdkDatabase,
                                         private val presentationNetworkOperations: PresentationNetworkOperations,
                                         private val issuanceNetworkOperations: IssuanceNetworkOperations) {

    private val cardDao = database.cardDao()

    suspend fun insert(portableIdentityCard: PortableIdentityCard) = cardDao.insert(portableIdentityCard)

    suspend fun delete(portableIdentityCard: PortableIdentityCard) = cardDao.delete(portableIdentityCard)

    fun getAllCards(): LiveData<List<PortableIdentityCard>> = cardDao.getAllCards()

    suspend fun getContract(url: String): Result<PicContract, Exception> = handleHttpResults(issuanceNetworkOperations.fetchContract(url), "fetch Contract")

    suspend fun getRequest(url: String) = handleHttpResults(presentationNetworkOperations.fetchRequestToken(url), "fetch request token")

    suspend fun sendIssuanceResponse(url: String, serializedResponse: String): Result<IssuanceServiceResponse, Exception> {
        return when (val httpResult = issuanceNetworkOperations.sendResponse(url, serializedResponse)) {
            is HttpResult.Success -> Result.Success(httpResult.body)
            is HttpResult.Error -> Result.Failure(IssuanceException("Server Error (${httpResult.code}): ${httpResult.body.code}."))
            is HttpResult.Failure -> Result.Failure(IssuanceException("Unable to send issuance response", httpResult.body))
        }
    }

    suspend fun sendPresentationResponse(url: String, serializedResponse: String) = handleHttpResults(presentationNetworkOperations.sendResponse(url, serializedResponse), "send presentation response")

    /**
     * Wrapper method to convert HttpResults to Results when dealing with Network Operations.
     * HttpResults are handled in this class in case we want to support retries based on status codes in the future.
     * We can implement retry for specific api in this class.
     */
    private fun <S, E> handleHttpResults(httpResult: HttpResult<S, E, Exception>, actionPerformed: String): Result<S, Exception> {
        return when (httpResult) {
            is HttpResult.Success -> Result.Success(httpResult.body)
            is HttpResult.Error -> Result.Failure(IssuanceException("Server Error: unable to $actionPerformed: (code: ${httpResult.code}) body: ${httpResult.body}."))
            is HttpResult.Failure -> Result.Failure(IssuanceException("Unable to $actionPerformed", httpResult.body))
        }
    }
}