package com.microsoft.did.sdk.crypto.keys
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.KeyUse

/**
 * Represents an OCT key
 * @class
 * @extends JsonWebKey
 */
class SecretKey(key: JsonWebKey) {
    /**
     * Set the Oct key type
     */
    var kty: KeyType = KeyType.Octets;

    /**
     * Key ID
     */
    open var kid: String? = key.kid

    /**
     * Intended use
     */
    open var use: KeyUse? = key.use?.let { KeyUse.valueOf(it) }

    /**
     * Valid key operations (key_ops)
     */
    open var key_ops: List<KeyUsage>? = key.key_ops?.map { KeyUsage.valueOf(it) }

    /**
     * Algorithm intended for use with this key
     */
    open var alg: String? = key.alg

    /**
     * secret
     */
    var k: String? = key.k

}