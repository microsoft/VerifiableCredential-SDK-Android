/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Class that can protect some content by signing.
 */
@Singleton
class OidcResponseSigner @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String
) {

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun sign(payload: String, keyReference: String = signatureKeyReference, additionalHeaders: Map<String, String> = emptyMap()): JwsToken {
        val token = JwsToken(payload)
        token.sign(keyReference, cryptoOperations, additionalHeaders)
        return token
    }
}