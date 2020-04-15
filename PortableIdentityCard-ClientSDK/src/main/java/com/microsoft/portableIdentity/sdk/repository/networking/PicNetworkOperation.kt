package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.ServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.apis.PortableIdentityCardApi
import com.microsoft.portableIdentity.sdk.utilities.controlflow.*
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicNetworkOperation @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val picApi: PortableIdentityCardApi = retrofit.create(PortableIdentityCardApi::class.java)

    /**
     * Get Contract from url.
     */
    suspend fun getContract(url: String): Result<PicContract> {
        val contract = fire(
            call = {picApi.getContract(url)},
            errorMessage = "Error Fetching Contract from $url."
        ) ?: return Result.Failure(NetworkException("Unable to fetch contrct"))
        return Result.Success(contract)
    }

    /**
     * Get Request from url.
     */
    suspend fun getRequestToken(url: String): Result<String> {
        val request = fire(
            call = {picApi.getRequest(url)},
            errorMessage = "Error Fetching Request from $url."
        ) ?: return Result.Failure(NetworkException("Unable to fetch request token."))
        return Result.Success(request)
    }

    /**
     * Post Response to url.
     */
    suspend fun sendIssuanceResponse(url: String, serializedResponse: String): Result<IssuanceServiceResponse?> {
        return try {
            val result = fire(
                call = {picApi.sendResponse(url, serializedResponse)},
                errorMessage = "Error Sending Response to $url."
            )
            Result.Success(result)
        } catch (exception: Exception) {
            Result.Failure(PresentationException("Failed to send Response", exception))
        }
    }

    /**
     * Post Presentation Response to url.
     */
    suspend fun sendPresentationResponse(url: String, serializedResponse: String): Result<String> {
        return try {
            val response = call(
                call = { picApi.sendPresentationResponse(url, serializedResponse) },
                errorMessage = "Error Sending Response to $url."
            )
            val requestUrl = response?.raw()?.request()?.url()
            Result.Success(requestUrl.toString())
        } catch (exception: Exception) {
            val presentationException = PresentationException("Failed to send Response", exception)
            Result.Failure(presentationException)
        }
    }
}