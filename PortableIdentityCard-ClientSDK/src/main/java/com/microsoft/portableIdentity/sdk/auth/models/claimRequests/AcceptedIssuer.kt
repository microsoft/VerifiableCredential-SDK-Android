/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.claimRequests

import kotlinx.serialization.Serializable

/**
 * Data Model for Accepted Issuer in OidcRequestContent.
 */
@Serializable
data class AcceptedIssuer(
    // did of the issuer.
    val iss: String
)