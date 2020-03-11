/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

/**
 * Class that can be used to validate, decrypt, and/or verify JoseToken.
 */
class Validator {

    fun validate(token: JoseToken): Boolean {
        // TODO: implement validate.
        return false
    }
}