/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.ProtocolType
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

/**
 * Factory that makes the correct Parser based on token.
 */
object ParserFactory {

    fun makeParser(protocolType: ProtocolType): Parser? {
        // TODO: implement factory method.
        return null
    }
}