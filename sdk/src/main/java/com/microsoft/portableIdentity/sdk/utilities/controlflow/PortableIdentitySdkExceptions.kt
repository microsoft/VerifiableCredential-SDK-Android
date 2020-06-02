/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.utilities.controlflow

open class PortableIdentitySdkException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class UnSupportedOperationException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

open class CryptoException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class KeyStoreException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class KeyException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class KeyFormatException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class AlgorithmException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class SignatureException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class EncodingException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class PairwiseKeyException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

class IdentifierCreatorException(message: String? = null, cause: Throwable? = null) : CryptoException(message, cause)

open class AuthenticationException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class PresentationException(message: String? = null, cause: Throwable? = null) : AuthenticationException(message, cause)

open class IssuanceException(message: String? = null, cause: Throwable? = null) : AuthenticationException(message, cause)

class PairwiseIssuanceException(message: String? = null, cause: Throwable? = null) : AuthenticationException(message, cause)

class ValidatorException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class ResolverException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class RegistrarException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

open class NetworkException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class ServiceUnreachableException(message: String? = null, cause: Throwable? = null) : NetworkException(message, cause)

class ServiceErrorException(message: String? = null, cause: Throwable? = null) : NetworkException(message, cause)

class UnauthorizedException(message: String? = null, cause: Throwable? = null) : NetworkException(message, cause)

class RepositoryException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)
