/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * Properties used to render a Logo.
 */
@Serializable
data class LogoDisplayProperties (

    // If image needs to be fetched, service will use this property.
    val uri: String? = null,

    // Else if image is in svg or base64 format, service will use this property.
    val image: String? = null,

    // Description used for alt text or voice over.
    val description: String
)