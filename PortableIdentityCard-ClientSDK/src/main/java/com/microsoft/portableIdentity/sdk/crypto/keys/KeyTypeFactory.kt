/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

/**
 * Factory class to create @enum KeyType objects
 */
object KeyTypeFactory {
    /**
     * Create the key type according to the selected algorithm.
     * @param algorithm Web crypto compliant algorithm object
     */
    fun createViaWebCrypto (algorithm: Algorithm): KeyType {
        return when (algorithm.name.toLowerCase()) {
            "hmac" -> KeyType.Octets
            "ecdsa", "ecdh" -> KeyType.EllipticCurve;
            "rsassa-pkcs1-v1_5", "rsa-oaep", "rsa-oaep-256" -> KeyType.RSA;
            else -> error("The algorithm '${algorithm.name}' is not supported");
        }
    }

    /**
     * Create the key use according to the selected algorithm.
     * @param algorithm JWA algorithm constant
     */
    fun createViaJwa (algorithm: String): KeyType {
        val alg = CryptoHelpers.jwaToWebCrypto(algorithm);
        return KeyTypeFactory.createViaWebCrypto(alg);
    }
}