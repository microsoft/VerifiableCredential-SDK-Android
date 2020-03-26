package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

/**
 * Data Model for Accepted Issuer in OidcRequestContent.
 */
@Serializable
data class AcceptedIssuer(
    // did of the issuer.
    val iss: String
)