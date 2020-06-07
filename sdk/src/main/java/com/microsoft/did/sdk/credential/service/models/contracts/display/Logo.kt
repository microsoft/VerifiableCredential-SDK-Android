/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * Properties used to render a Logo.
 */
@Serializable
data class Logo(

    // If image needs to be fetched, service will use this property.
    val uri: String = "",

    // Else if image is in svg or base64 format, service will use this property.
    val image: String = "",

    // Description used for alt text or voice over.
    val description: String = ""
)