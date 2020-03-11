/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.parsers.Parser
import com.microsoft.portableIdentity.sdk.auth.parsers.ParserFactory
import com.microsoft.portableIdentity.sdk.auth.validators.Validator
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

/**
 * Class that represents a generic Request.
 */
class Request(val rawRequest: JoseToken, val protocolType: ProtocolType, val parser: Parser) {

    val validator: Validator = Validator()

    companion object {
        /**
         * Create Request object from rawRequest.
         */
        fun create(rawRequest: JoseToken): Request? {
            val protocolType = ProtocolType.getProtocolType(rawRequest) ?: return null
            val parser = ParserFactory.makeParser(protocolType) ?: return null
            return Request(rawRequest, protocolType, parser)
        }
    }

    /**
     * Validate this request using the Validator.
     *
     * Return: true if successfully validate.
     */
    fun validate(): Boolean {
        return validator.validate(rawRequest)
    }

    /**
     * Get Credential Requests.
     */
    fun getCredentialRequests(): CredentialRequests {
        return parser.getCredentialRequests()
    }
}