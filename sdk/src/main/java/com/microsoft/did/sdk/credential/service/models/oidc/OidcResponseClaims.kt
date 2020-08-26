// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.Constants
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
    var publicKeyJwk: JsonWebKey = JsonWebKey()

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