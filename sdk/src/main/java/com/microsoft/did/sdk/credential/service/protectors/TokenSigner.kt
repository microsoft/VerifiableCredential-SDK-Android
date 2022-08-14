/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenSigner @Inject constructor(
    private val keyStore: EncryptedKeyStore
) {
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        val token = JwsToken(payload, JWSAlgorithm.ES256K)
        // adding kid value to header.
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .type(JOSEObjectType.JWT)
            .keyID("${identifier.id}#${identifier.signatureKeyReference}")
            .build()
        val privateKey = keyStore.getKey(identifier.signatureKeyReference)
        token.sign(privateKey, header)
        return token.serialize()
    }
}