/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.Constants
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of an OpenID Self-Issued Token Response.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class OidcResponseContentForIssuance(
    @Required
    val iss: String = Constants.SELF_ISSUED,

    // thumbprint (sha-256) of the public key
    val sub: String = "",

    // url that is meant to receive the response.
    val aud: String = "",

    // did tied to the private key that signed response.
    val did: String,

    // the public key that can be used to verify signature.
    @SerialName(Constants.SUB_JWK)
    val subJwk: JsonWebKey,

    // time the token was issued.
    val iat: Long,

    // time the token expires.
    val exp: Long,

    //id of the response
    val jti: String,

    // VC service specific
    // response contains claims that fulfills this contract.
    val contract: String? = null,

    // attestations that were asked for in request.
    val attestations: AttestationClaimModel? = null
)