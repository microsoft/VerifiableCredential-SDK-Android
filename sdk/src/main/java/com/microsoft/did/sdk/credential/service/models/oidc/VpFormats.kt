/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationRequestFormatSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines the formats supported by the verifier.
 */
@Serializable
data class VpFormats(
    @Serializable(with = PresentationRequestFormatSerializer::class)
    @SerialName("jwt_vp")
    val jwtVp: List<String> = emptyList(),

    @Serializable(with = PresentationRequestFormatSerializer::class)
    @SerialName("jwt_vc")
    val jwtVc: List<String> = emptyList()
)