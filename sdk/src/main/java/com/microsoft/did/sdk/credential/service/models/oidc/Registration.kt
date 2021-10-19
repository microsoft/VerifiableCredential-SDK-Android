/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationRequestFormatSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object for relying parties to give user more details about themselves.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html)
 */
@Serializable
data class Registration(
    @SerialName("client_name")
    val clientName: String = "",

    @SerialName("client_purpose")
    val clientPurpose: String = "",

    @SerialName("tos_uri")
    val termsOfServiceUrl: String = "",

    @SerialName("logo_uri")
    val logoUri: String = "",

    @SerialName("client_uri")
    val clientUri: String = "",

    @SerialName("subject_identifier_types_supported")
    val subjectIdentifierTypesSupported: List<String> = emptyList(),

    @SerialName("did_methods_supported")
    val didMethodsSupported: List<String> = emptyList(),

    @Serializable(with = PresentationRequestFormatSerializer::class)
    @SerialName("vp_formats")
    val vpFormats: List<String> = emptyList()
)