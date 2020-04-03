/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.oidc

import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.Registration
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val RESPONSE_TYPE = "response_type"
const val RESPONSE_MODE = "response_mode"
const val CLIENT_ID = "client_id"
const val REDIRECT_URL = "redirect_uri"
const val MAX_AGE = "max_age"

/**
 * Contents of an OpenID Self-Issued Token Request.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class OidcRequestContent(

    // what type of object the response should be (should be idtoken).
    @SerialName(RESPONSE_TYPE)
    val responseType: String = "",

    // what mode the response should be sent in (should always be form post).
    @SerialName(RESPONSE_MODE)
    val responseMode: String = "",

    // did of the entity who sent the request.
    @SerialName(CLIENT_ID)
    val clientId: String = "",

    // where the SIOP provider should send response to.
    @SerialName(REDIRECT_URL)
    val redirectUrl: String = "",

    val iss: String = "",

    // should contain "openid did_authn"
    val scope: String = "",

    // opaque values that should be passed back to the requester.
    val state: String = "",
    val nonce: String = "",

    // Claims that are being requested.
    val attestations: CredentialAttestations? = null,

    // iat, nbf, and exp that need to be checked to see if token has expired
    val exp: Long = 0,
    val iat: Long = 0,
    val nbf: Long = 0,

    // optional parameters
    val registration: Registration? = null,
    val aud: String = "",
    @SerialName(MAX_AGE)
    val maxAge: Int = 0
)