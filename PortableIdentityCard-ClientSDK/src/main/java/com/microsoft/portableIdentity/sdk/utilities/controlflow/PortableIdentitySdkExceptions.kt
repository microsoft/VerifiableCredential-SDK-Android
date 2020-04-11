/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.utilities.controlflow

abstract class PortableIdentitySdkException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class CryptoException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class AuthenticationException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class ResolverException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)

class RegistrarException(message: String? = null, cause: Throwable? = null) : PortableIdentitySdkException(message, cause)