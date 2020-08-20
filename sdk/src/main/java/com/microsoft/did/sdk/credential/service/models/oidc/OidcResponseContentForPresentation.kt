/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationSubmission
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
data class OidcResponseContentForPresentation(

    // thumbprint (sha-256) of the public key
    val sub: String = "",

    // url that is meant to receive the response.
    val aud: String = "",

    // nonce from the request.
    val nonce: String = "",

    // state from the request.
    val state: String = "",

    // did tied to the private key that signed response.
    val did: String = "",

    // the public key that can be used to verify signature.
    @SerialName("sub_jwk")
    val publicKeyJwk: JsonWebKey = JsonWebKey(),

    @SerialName("iat")
    val responseCreationTime: Long = 0,

    @SerialName("exp")
    val expirationTime: Long = 0,

    @SerialName("jti")
    val responseId: String = "",

    @SerialName("presentation_submission")
    var presentationSubmission: CredentialPresentationSubmission = CredentialPresentationSubmission(),

    // attestations that were asked for in request.
    var attestations: AttestationClaimModel = AttestationClaimModel(),

    @Required
    @SerialName("iss")
    val issuer: String = Constants.SELF_ISSUED
)