package com.microsoft.portableIdentity.sdk.auth.models.oidc

import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcResponse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OidcResponseContent(
    @Required
    val iss: String = OidcResponse.SELFISSUED,
    val sub: String, // thumbprint (sha-256)
    val aud: String,
    val nonce: String,
    val did: String?,
    @SerialName("sub_jwk")
    val subJwk: JsonWebKey,
    val iat: Int,
    val exp: Int,

    val state: String? = null,
    @SerialName("_claim_names")
    val claimNames: Map<String, String>? = null,
    @SerialName("_claim_sources")
    val claimSources: Map<String, List<Map<String, String>>>? = null,

    val schema: String? = null
)