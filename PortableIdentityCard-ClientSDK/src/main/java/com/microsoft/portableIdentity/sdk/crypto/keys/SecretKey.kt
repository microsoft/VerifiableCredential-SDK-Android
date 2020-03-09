package com.microsoft.portableIdentity.sdk.crypto.keys
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.toKeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.toKeyUsage
import com.microsoft.portableIdentity.sdk.utilities.ILogger

/**
 * Represents an OCT key
 * @class
 * @extends JsonWebKey
 */
open class SecretKey(key: JsonWebKey, logger: ILogger): IKeyStoreItem {
    /**
     * Set the Oct key type
     */
    var kty: KeyType = KeyType.Octets

    /**
     * Key ID
     */
    override var kid: String = key.kid ?: ""

    /**
     * Intended use
     */
    open var use: KeyUse? = key.use?.let { toKeyUse(it) }

    /**
     * Valid key operations (key_ops)
     */
    open var key_ops: List<KeyUsage>? = key.key_ops?.map { toKeyUsage(it, logger = logger) }

    /**
     * Algorithm intended for use with this key
     */
    open var alg: String? = key.alg

    /**
     * secret
     */
    var k: String? = key.k

    fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = kty.value,
            kid = kid,
            use = use?.value,
            alg = alg,
            key_ops = key_ops?.map { use -> use.value },
            k = k
        )
    }

}