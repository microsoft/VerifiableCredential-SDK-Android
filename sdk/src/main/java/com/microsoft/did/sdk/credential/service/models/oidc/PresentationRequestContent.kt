/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of an OpenID Self-Issued Token Request.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class PresentationRequestContent(
    // what type of object the response should be (should be idtoken). // TODO: validate
    @SerialName("response_type")
    val responseType: String = "",

    // what mode the response should be sent in (should always be form post). // TODO: validate
    @SerialName("response_mode")
    val responseMode: String = "",

    @SerialName("client_id")
    val clientId: String = "",

    @SerialName("redirect_uri")
    val redirectUrl: String = "",

    @SerialName("iss")
    val issuer: String = "",

    // should contain "openid did_authn" // TODO: validate
    val scope: String = "",

    val state: String = "",

    val nonce: String = "",

    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition = PresentationDefinition(),

    @SerialName("exp")
    val expirationTime: Long = 0,

    @SerialName("iat")
    val idTokenCreationTime: Long = 0,

    @SerialName("nbf")
    val notValidBefore: Long = 0,

    // if set to "create", request is just for issuance.
    val prompt: String = "",

    @SerialName("aud")
    val audience: String = "",

    @SerialName("max_age")
    val maxAge: Int = 0,

    var registration: Registration = Registration()
)
