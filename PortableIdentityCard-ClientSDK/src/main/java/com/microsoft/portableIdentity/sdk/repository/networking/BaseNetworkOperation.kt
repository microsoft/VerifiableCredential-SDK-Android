package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.utilities.controlflow.*
import kotlinx.io.IOException
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

abstract class BaseNetworkOperation<S> {

    abstract val call: suspend () -> Response<S>

    val isRetryable: Boolean = false

    open suspend fun fire(url: String): Result<S, PortableIdentitySdkException> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                return onSuccess(response)
            }
            return onFailure(response)
        } catch (exception: IOException) {
            return Result.Failure(NetworkException("Failed to send request.", exception))
        }
    }

    open fun onSuccess(response: Response<S>): Result<S, PortableIdentitySdkException> {
        // TODO("how do we want to handle null bodies")
        return Result.Success(response.body() ?: throw NetworkException("Body of Response is null."))
    }

    // TODO("what do we want our base to look like")
    open fun onFailure(response: Response<S>): Result<S, PortableIdentitySdkException> {
        return when (response.code()) {
            401 -> Result.Failure(UnauthorizedException(response.message()))
            402, 403, 404 -> Result.Failure(ServiceErrorException(response.message()))
            500, 501, 502, 503 -> Result.Failure(ServiceUnreachableException(response.message()))
            else -> Result.Failure(NetworkException("Unknown Status code ${response.code()}"))
        }
    }

    fun <S> onRetry(): Result<S, PortableIdentitySdkException> {
        throw NetworkException("Retry Not Supported.")
    }
}