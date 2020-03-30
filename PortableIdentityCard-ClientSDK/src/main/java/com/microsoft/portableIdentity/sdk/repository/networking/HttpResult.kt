package com.microsoft.portableIdentity.sdk.repository.networking

/**
 * Class to handle Network response.
 * It either can be Success with the required data or Error with an exception.
 */
sealed class HttpResult<out T: Any> {
    data class Success<out T : Any>(val data: T) : HttpResult<T>()
    data class Error(val exception: Exception) : HttpResult<Nothing>()
}