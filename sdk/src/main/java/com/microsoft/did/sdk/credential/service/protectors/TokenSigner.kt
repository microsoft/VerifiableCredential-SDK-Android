/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PRESENTATION_FORMAT
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can protect some content by signing.
 */
@Singleton
class TokenSigner @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Json
) {

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        val token = JwsToken(payload, serializer)
        val kid = cryptoOperations.keyStore.getPrivateKey(identifier.signatureKeyReference).getKey().kid
        // adding kid value to header.
        val additionalHeaders = mutableMapOf<String, String>()
        additionalHeaders[JoseConstants.Kid.value] = "${identifier.id}${kid}"
        additionalHeaders[JoseConstants.Type.value] = CREDENTIAL_PRESENTATION_FORMAT
        token.sign(identifier.signatureKeyReference, cryptoOperations, additionalHeaders)
        return token.serialize(serializer)
    }
}