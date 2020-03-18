/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import javax.xml.transform.Templates

/**
 * Class that can be used to validate, decrypt, and/or verify JoseToken.
 */
class Validator: IValidator {

    override suspend fun verify(token: JwsToken, requester: String): Boolean {
        val cryptoOperations = DidSdkConfig.didManager.cryptoOperations
        val requesterDidDocument = DidSdkConfig.didManager.resolver.resolve(requester, cryptoOperations)
        val keys = requesterDidDocument.document.publicKeys.map {
            it.toPublicKey(BaseLogger)
        }
        return token.verify(cryptoOperations, keys)
    }
}