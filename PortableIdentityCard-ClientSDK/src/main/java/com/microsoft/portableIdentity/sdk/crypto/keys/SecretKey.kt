package com.microsoft.portableIdentity.sdk.crypto.keys
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import androidx.room.util.StringUtil
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.toKeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.toKeyUsage
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.byteArrayToString

/**
 * Represents an OCT key
 * Used for storing seeds.
 * @class
 */
open class SecretKey(val k: ByteArray, override val kid: String): IKeyStoreItem {

    /**
     * Set the Oct key type
     */
    val kty: KeyType = KeyType.Octets

    fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = kty.value,
            kid = kid,
            k = byteArrayToString(k)
        )
    }

}