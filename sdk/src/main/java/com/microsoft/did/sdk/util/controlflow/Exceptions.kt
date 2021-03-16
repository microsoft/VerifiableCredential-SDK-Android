/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util.controlflow

open class SdkException(message: String? = null, cause: Throwable? = null, val retryable: Boolean = false) : Exception(message, cause)

open class CryptoException(message: String, cause: Throwable? = null, retryable: Boolean = false) : SdkException(message, cause, retryable)

class KeyStoreException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

open class AuthenticationException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    SdkException(message, cause, retryable)

open class PresentationException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    AuthenticationException(message, cause, retryable)

open class IssuanceException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    AuthenticationException(message, cause, retryable)

class ExchangeException(message: String, cause: Throwable? = null) : PresentationException(message, cause)

open class RevocationException(message: String? = null, cause: Throwable? = null, retryable: Boolean = true) :
    SdkException(message, cause, retryable)

open class ValidatorException(message: String, cause: Throwable? = null, retryable: Boolean = false) :
    SdkException(message, cause, retryable)

class InvalidSignatureException(message: String) : ValidatorException(message)

class ExpiredTokenException(message: String) : ValidatorException(message)

class InvalidResponseTypeException(message: String) : ValidatorException(message)

class InvalidResponseModeException(message: String) : ValidatorException(message)

class InvalidScopeException(message: String) : ValidatorException(message)

class MissingInputInRequestException(message: String) : ValidatorException(message)

class DidInHeaderAndPayloadNotMatching(message: String) : ValidatorException(message)

open class ResolverException(message: String, cause: Throwable? = null) : SdkException(message, cause)

class LinkedDomainEndpointInUnknownFormatException(message: String, cause: Throwable? = null) : ResolverException(message, cause)

class RegistrarException(message: String, cause: Throwable? = null) : SdkException(message, cause)

open class LocalNetworkException(message: String, cause: Throwable? = null) : SdkException(message, cause, true)

open class NetworkException(message: String, retryable: Boolean) : SdkException(message, null, retryable) {
    var requestId: String? = null
    var correlationVector: String? = null
    var errorCode: String? = null
    var errorBody: String? = null
    var innerErrors: String? = null
}

class ServiceUnreachableException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class ClientException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class ForbiddenException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class NotFoundException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class UnauthorizedException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class RedirectException(message: String, retryable: Boolean) : NetworkException(message, retryable)

class RepositoryException(message: String, cause: Throwable? = null) : SdkException(message, cause)

class InvalidImageException(message: String, cause: Throwable? = null) : SdkException(message, cause)