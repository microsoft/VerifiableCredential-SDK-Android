/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.utilities

object Constants {

    // OIDC Protocol Constants
    const val RESPONSE_TYPE = "response_type"
    const val RESPONSE_MODE = "response_mode"
    const val CLIENT_ID = "client_id"
    const val REDIRECT_URL = "redirect_uri"
    const val MAX_AGE = "max_age"
    const val SELF_ISSUED = "https://self-issued.me"
    const val SUB_JWK = "sub_jwk"
    const val CLAIM_NAMES = "_claim_names"
    const val CLAIM_SOURCES = "_claim_sources"

    // General Constants
    const val MILLISECONDS_IN_A_SECOND = 1000
    const val SECONDS_IN_A_MINUTE = 60
    const val RESPONSE_EXPIRATION_IN_MINUTES = 5

    //Portable Identity Constants
    const val IDENTITY_SECRET_KEY_NAME = "did.identifier"
    const val METHOD_NAME = "ion"
    const val INITIAL_STATE_LONGFORM = "-$METHOD_NAME-initial-state"
    const val SIDETREE_OPERATION_TYPE = "create"
    const val SIDETREE_PATCH_ACTION = "replace"
}