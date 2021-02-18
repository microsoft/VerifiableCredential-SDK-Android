/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import android.util.Base64

object Constants {

    // OIDC Protocol Constants
    const val SELF_ISSUED = "https://self-issued.me"
    const val PURE_ISSUANCE_FLOW_VALUE = "create"
    const val RESPONSE_TYPE = "id_token"
    const val RESPONSE_MODE = "form_post"
    const val SCOPE = "openid did_authn"

    //Presentation Exchange Constants
    const val CREDENTIAL_PATH_IN_RESPONSE = "$.attestations.presentations"
    const val CREDENTIAL_PRESENTATION_FORMAT = "JWT"
    const val CREDENTIAL_PRESENTATION_ENCODING = "base64Url"

    //Verifiable Credential Constants
    const val CONTEXT = "@context"
    const val VP_CONTEXT_URL = "https://www.w3.org/2018/credentials/v1"
    const val VERIFIABLE_PRESENTATION_TYPE = "VerifiablePresentation"
    const val VERIFIABLE_CREDENTIAL_DEFAULT_TYPE = "VerifiableCredential"

    //Well-Known Config Document Constants
    const val WELL_KNOWN_CONFIG_DOCUMENT_LOCATION = ".well-known/did-configuration.json"
    const val LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE = "LinkedDomains"

    // General Constants
    const val MILLISECONDS_IN_A_SECOND = 1000
    const val SECONDS_IN_A_MINUTE = 60
    const val DEFAULT_EXPIRATION_IN_SECONDS = 3600
    const val DEFAULT_VP_EXPIRATION_IN_SECONDS = 3600
    const val HASHING_ALGORITHM_FOR_ID = "MD5"

    const val DEEP_LINK_SCHEME = "openid"
    const val DEEP_LINK_HOST = "vc"
    const val CORRELATION_VECTOR_IN_PREF = "correlation_vector"

    //Identifier Constants
    const val MAIN_IDENTIFIER_REFERENCE = "did.main.identifier"
    const val METHOD_NAME = "ion"
    const val IDENTIFIER_PUBLIC_KEY_PURPOSE = "authentication"
    const val COLON = ":"
    const val SIDETREE_PATCH_ACTION = "replace"
    const val SIDETREE_MULTIHASH_CODE = 18
    const val SIDETREE_MULTIHASH_LENGTH = 32
    const val SECP256K1_CURVE_NAME_EC = "secp256k1"

    // Header Constants for network calls
    const val USER_AGENT_HEADER = "User-Agent"
    const val CORRELATION_VECTOR_HEADER = "ms-cv"
    const val REQUEST_ID_HEADER = "request-id"

    //  Base64 Encoding flags
    const val BASE64_URL_SAFE = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    const val AES_KEY = "AES"
    const val SEED_BYTES = 32
}