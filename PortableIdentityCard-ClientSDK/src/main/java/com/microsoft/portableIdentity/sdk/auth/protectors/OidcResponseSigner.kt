/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.AuthenticationConstants.RESPONSE_EXPIRATION_IN_MINUTES
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.floor

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
    fun sign(payload: String, keyReference: String, additionalHeaders: Map<String, String> = emptyMap()): JwsToken {
        val token = JwsToken(payload)
        token.sign(keyReference, cryptoOperations, additionalHeaders)
        return token
    }
}