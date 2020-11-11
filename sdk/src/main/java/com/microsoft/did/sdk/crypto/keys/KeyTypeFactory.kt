/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.util.controlflow.UnSupportedAlgorithmException
import java.util.Locale

/**
 * Factory class to create @enum KeyType objects
 */
object KeyTypeFactory {
    /**
     * Create the key type according to the selected algorithm.
     * @param algorithm Web crypto compliant algorithm object
     */
    fun createViaWebCrypto(algorithm: Algorithm): KeyType {
        return when (algorithm.name.toLowerCase(Locale.ENGLISH)) {
            "hmac" -> KeyType.Octets
            "ecdsa", "ecdh" -> KeyType.EllipticCurve
            "rsassa-pkcs1-v1_5", "rsa-oaep", "rsa-oaep-256" -> KeyType.RSA
            else -> throw UnSupportedAlgorithmException("The algorithm '${algorithm.name}' is not supported")
        }
    }
}