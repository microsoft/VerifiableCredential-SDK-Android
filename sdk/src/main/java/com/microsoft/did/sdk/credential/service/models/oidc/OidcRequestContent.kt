/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationDefinition
import com.microsoft.did.sdk.util.Constants.CLIENT_ID
import com.microsoft.did.sdk.util.Constants.MAX_AGE
import com.microsoft.did.sdk.util.Constants.REDIRECT_URL
import com.microsoft.did.sdk.util.Constants.RESPONSE_MODE
import com.microsoft.did.sdk.util.Constants.RESPONSE_TYPE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of an OpenID Self-Issued Token Request.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class OidcRequestContent(

    // what type of object the response should be (should be idtoken). // TODO: validate
    @SerialName("response_type")
    val responseType: String = "",

    // what mode the response should be sent in (should always be form post). // TODO: validate
    @SerialName("response_mode")
    val responseMode: String = "",

    // did of the entity who sent the request.
    @SerialName("client_id")
    val clientId: String = "",

    // where the SIOP provider should send response to.
    @SerialName("redirect_uri")
    val redirectUrl: String = "",

    // did of the entity who sent the request.
    @SerialName("iss")
    val iss: String = "",

    // should contain "openid did_authn"
    val scope: String = "",

    // opaque values that should be passed back to the requester.
    val state: String = "",
    val nonce: String = "",

    // Claims that are being requested.
    @SerialName("presentation_definition")
    val credentialPresentationDefinition: CredentialPresentationDefinition? = null,

    @SerialName("exp")
    val expirationTime: Long = 0,

    @SerialName("iat")
    val idTokenCreationTime: Long = 0,

    @SerialName("nbf")
    val nbf: Long = 0,

    // if set to "create", request is just for issuance.
    val prompt: String = "",

    // object for relying parties to give user more details about themselves.
    val registration: Registration? = null,

    // optional parameters
    val aud: String = "",
    @SerialName(MAX_AGE)
    val maxAge: Int = 0
)