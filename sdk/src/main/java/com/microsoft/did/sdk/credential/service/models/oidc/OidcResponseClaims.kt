// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.crypto.protocols.jose.jws.serialization.JwkSerializer
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class OidcResponseClaims {
    @SerialName("sub")
    var publicKeyThumbPrint: String = ""

    @SerialName("aud")
    var audience: String = ""

    var did: String = ""

    @SerialName("sub_jwk")
    @Serializable(with = JwkSerializer::class)
    var publicKeyJwk: JWK? = null

    @SerialName("iat")
    var responseCreationTime: Long = 0

    @SerialName("exp")
    var responseExpirationTime: Long = 0

    @SerialName("jti")
    var responseId: String = ""

    @Required
    @SerialName("iss")
    var issuer: String = Constants.SELF_ISSUED
}