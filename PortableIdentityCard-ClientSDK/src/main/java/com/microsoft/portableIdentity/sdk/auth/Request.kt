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
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger

/**
 * Class that represents a generic Request.
 * As of now, only support rawRequests of JoseToken.
 */
class Request private constructor(val token: JwsToken,
                                  val contents: RequestContent,
                                  val protocolType: ProtocolType,
                                  private val validator: IValidator) {

    companion object {
        /**
         * Create Method to create a Request object from a raw request string.
         *
         * @param rawRequest String contains a request.
         * @param validator optional validator param for dependency injection.
         * @param parser optional parser param for dependency injection.
         *
         * @return a Request object.
         */
        fun create(rawRequest: String,
                   validator: IValidator = Validator(),
                   parser: IParser = Parser()): Request {

            try {
                val token = JwsToken.deserialize(rawRequest, BaseLogger)
                val (contents, protocolType) = parser.parse(token)
                return Request(token, contents, protocolType, validator)
            } catch (exception: Exception) {
                BaseLogger.log("raw request is not a JwsToken.")
            }
                throw BaseLogger.error("Other raw request types such as JweToken not supported.")
        }
    }

    /**
     * Validate this request using the Validator.
     * This includes:
     * 1. verifying the signature of the JWSToken
     * 2. check the claims of content protocol.
     *
     * Return: true if successfully validate.
     */
    suspend fun validate(): Boolean {
        return validator.verify(token, contents.requester) && contents.isValid()
    }

    /**
     * Get Credential Requests if there are any in Request.
     */
    fun getCredentialRequests(): CredentialRequests? {
        return contents.getCredentialRequests()
    }
}