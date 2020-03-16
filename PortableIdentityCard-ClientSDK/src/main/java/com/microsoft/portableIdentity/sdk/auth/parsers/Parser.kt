/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.ProtocolType
import com.microsoft.portableIdentity.sdk.auth.models.oidc.SIOPRequestContent
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * Object that Parses contents of JwsTokens into correct object.
 */
class Parser() : IParser {

    override fun parse(token: JwsToken): Pair<RequestContent, ProtocolType> {
        try {
            val contents = Serializer.parse(SIOPRequestContent.serializer(), token.content())
            return Pair(contents, ProtocolType.OIDC)
        } catch (exception: Exception) {
            BaseLogger.log("token is not an OIDC Request.")
        }
        throw Exception("Protocol Not Supported")
    }
}