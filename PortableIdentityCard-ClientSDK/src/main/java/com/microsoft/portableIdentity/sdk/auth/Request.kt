/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.parsers.Parser
import com.microsoft.portableIdentity.sdk.auth.parsers.IParser
import com.microsoft.portableIdentity.sdk.auth.validators.IValidator
import com.microsoft.portableIdentity.sdk.auth.validators.Validator
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

/**
 * Class that represents a generic Request.
 * As of now, only support rawRequests of JoseToken.
 */
class Request(val rawRequest: JoseToken,
              val parser: IParser = Parser(),
              val validator: IValidator = Validator()) {

    /**
     * The contents of the RawRequest
     */
    var contents: RequestContent? = null

    /**
     * The type of protocol that the contents conform to.
     */
    var protocolType: ProtocolType? = null

    /**
     * Validate this request using the Validator.
     * This includes:
     * 1. TODO: decrypting if JWEToken
     * 2. verifying the signature if contains JWSToken
     * 3. TODO: checking the claims of content protocol.
     *
     * Return: true if successfully validate.
     */
    fun validate(): Boolean {
        if (rawRequest is JwsToken) {
            return validator.verify(rawRequest)
        }
        return false
    }

    /**
     * Parses contents of the JoseToken and sets the contents and protocolType properties.
     * TODO: add decrypting token if needed via validator.
     */
    fun parseContents() {
        if (rawRequest is JwsToken) {
            val (contents, protocolType) = parser.parse(rawRequest)
            this.contents = contents
            this.protocolType = protocolType
        }
        else {
            throw Exception("JoseToken type not supported")
        }
    }

    /**
     * Get Credential Requests.
     */
    fun getCredentialRequests(): CredentialRequests? {
        if (contents == null) {
            parseContents()
        }
        return contents?.getCredentialRequests()
    }
}