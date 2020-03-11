/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

enum class ProtocolType {
    OIDC,
    SIOP;

    companion object {
        fun getProtocolType(rawRequest: JoseToken): ProtocolType? {
            // TODO: implement this switch statement correctly
            return SIOP
        }
    }
}