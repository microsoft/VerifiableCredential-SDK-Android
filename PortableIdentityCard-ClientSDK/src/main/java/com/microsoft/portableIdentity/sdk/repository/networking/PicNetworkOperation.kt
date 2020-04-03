package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.ServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.apis.PortableIdentityCardApi
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicNetworkOperation @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val picApi: PortableIdentityCardApi = retrofit.create(PortableIdentityCardApi::class.java)

    /**
     * Get Contract from url.
     */
    suspend fun getContract(url: String): PicContract? {
        return fire(
            call = {picApi.getContract(url)},
            errorMessage = "Error Fetching Contract from $url."
        )
    }

    /**
     * Get Request from url.
     */
    suspend fun getRequest(url: String): String? {
        return fire(
            call = {picApi.getRequest(url)},
            errorMessage = "Error Fetching Request from $url."
        )
    }

    /**
     * Post Response to url.
     */
    suspend fun sendResponse(url: String, serializedResponse: String): ServiceResponse? {
        return fire(
            call = {picApi.sendResponse(url, serializedResponse)},
            errorMessage = "Error Sending Response to $url."
        )
    }
}