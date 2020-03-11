package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Registration(
    @SerialName("redirect_uris")
    val redirectUris: List<String>?,
    @SerialName("response_types")
    val responseTypes: List<String>?,
    @SerialName("grant_types")
    val grantTypes: List<String>?,
    @SerialName("application_type")
    val applicationType: String?,
    val contacts: List<String>?,
    @SerialName("client_name")
    val clientName: String?,
    @SerialName("logo_uri")
    val logoUri: String?,
    @SerialName("client_uri")
    val clientUri: String?,
    @SerialName("policy_uri")
    val policyUri: String?,
    @SerialName("tos_uri")
    val TermsOfServiceUri: String?,
    @SerialName("Jwks_uri")
    val JsonWebKeySetUri: String?,
//    @SerialName("jwks")
//    val JsonWebKeySet: TODO: Implement JsonWebKeySet,
    @SerialName("request_uris")
    val requestUris: List<String>?
)