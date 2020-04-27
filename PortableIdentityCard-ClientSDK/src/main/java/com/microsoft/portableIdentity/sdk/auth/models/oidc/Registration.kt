package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

/**
 * Object for relying parties to give user more details about themselves.
 */
@Serializable
data class Registration(

    // name of the relying party.
    val clientName: String = "",

    // purpose of the presentation request.
    val clientPurpose: String = "",

    // terms of service url that a user can press on to see terms and service in webView.
    val termsOfServiceUrl: String = "",

    // logo uri if relying party wants to display their logo in presentation prompt.
    val logoUri: String = ""
)