/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

object Constants {

    // OIDC Protocol Constants
    const val RESPONSE_TYPE = "response_type"
    const val RESPONSE_MODE = "response_mode"
    const val CLIENT_ID = "client_id"
    const val REDIRECT_URL = "redirect_uri"
    const val MAX_AGE = "max_age"
    const val SELF_ISSUED = "https://self-issued.me"
    const val SUB_JWK = "sub_jwk"

    // OIDC Registration Constants
    const val CLIENT_NAME = "client_name"
    const val CLIENT_PURPOSE = "client_purpose"
    const val TERMS_AND_SERVICES_URI = "tos_uri"
    const val LOGO_URI = "logo_uri"

    //Verifiable Credential Constants
    const val CONTEXT = "@context"
    const val VP_CONTEXT_URL = "https://www.w3.org/2018/credentials/v1"
    const val VERIFIABLE_PRESENTATION_TYPE = "VerifiablePresentation"

    // General Constants
    const val MILLISECONDS_IN_A_SECOND = 1000
    const val SECONDS_IN_A_MINUTE = 60
    const val DEFAULT_EXPIRATION_IN_MINUTES = 3000
    const val RESPONSE_EXPIRATION_IN_MINUTES = 600
    const val HASHING_ALGORITHM_FOR_ID = "MD5"

    //Identifier Constants
    const val IDENTIFIER_SECRET_KEY_NAME = "did.identifier"
    const val METHOD_NAME = "ion"
    const val INITIAL_STATE_LONGFORM = "-$METHOD_NAME-initial-state"
    const val SIDETREE_OPERATION_TYPE = "create"
    const val SIDETREE_PATCH_ACTION = "replace"
    const val SIDETREE_MULTIHASH_CODE = 18
    const val SIDETREE_MULTIHASH_LENGTH = 32
    const val SIDETREE_CURVE_NAME_FOR_EC = "secp256k1"
    const val SIGNATURE_KEYREFERENCE = "sign"
    const val RECOVERY_KEYREFERENCE = "recover"
}