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

/**
 * Class that can protect some content by signing.
 */
@Singleton
class TokenSigner @Inject constructor() {

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        val token = JwsToken(payload)
        // adding kid value to header.
        token.setKeyId("${identifier.id}#${identifier.signatureKeyReference}")
        token.setType(JOSEObjectType.JWT)
        token.sign(identifier.signatureKeyReference, EncryptedKeyStore.keyStore)
        return token.serialize()
    }
}