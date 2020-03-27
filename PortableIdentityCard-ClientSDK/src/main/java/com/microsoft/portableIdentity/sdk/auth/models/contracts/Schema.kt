/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.contracts

import kotlinx.serialization.Serializable

/**
 * A structured data model used to describe the set of claims in a Verifiable Credential.
 */
@Serializable
data class Schema (

    // Should be set to "schema".
    val id: String = "schema"
)