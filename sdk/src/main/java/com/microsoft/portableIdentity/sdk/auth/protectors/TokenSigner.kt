/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Class that can protect some content by signing.
 */
@Singleton
class TokenSigner @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer
) {

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        val token = makeJwsToken(payload)
        val kid = cryptoOperations.keyStore.getPrivateKey(identifier.signatureKeyReference).getKey().kid
        // adding kid value to header.
        val additionalHeaders = mutableMapOf<String, String>()
        additionalHeaders[JoseConstants.Kid.value] = "${identifier.id}${kid}"
        token.sign(identifier.signatureKeyReference, cryptoOperations, additionalHeaders)
        return token.serialize(serializer)
    }

    internal fun makeJwsToken(payload: String): JwsToken {
        return JwsToken(payload, serializer)
    }
}