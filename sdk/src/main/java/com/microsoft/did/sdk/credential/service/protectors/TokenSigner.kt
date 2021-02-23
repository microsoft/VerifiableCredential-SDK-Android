/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.JOSEObjectType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenSigner @Inject constructor(
    private val keyStore: EncryptedKeyStore
) {
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        val token = JwsToken(payload)
        // adding kid value to header.
        token.setKeyId("${identifier.id}#${identifier.signatureKeyReference}")
        token.setType(JOSEObjectType.JWT)
        val privateKey = keyStore.getKey(identifier.signatureKeyReference)
        token.sign(privateKey)
        return token.serialize()
    }
}