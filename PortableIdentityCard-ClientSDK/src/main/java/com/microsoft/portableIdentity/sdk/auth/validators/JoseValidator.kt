/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger

/**
 * Class that can be used to validate, decrypt, and/or verify JoseToken.
 */
class JoseValidator: Validator {

    override suspend fun verify(token: JwsToken, requester: String): Boolean {
        val cryptoOperations = DidSdkConfig.didManager.cryptoOperations
        val requesterDidDocument = DidSdkConfig.didManager.resolver.resolve(requester, cryptoOperations)
        val keys = requesterDidDocument.document.publicKeys.map {
            it.toPublicKey(BaseLogger)
        }
        return token.verify(cryptoOperations, keys)
    }
}