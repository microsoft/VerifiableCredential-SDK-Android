/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts

import kotlinx.serialization.Serializable

const val SCHEMA = "schema"

/**
 * A structured data model used to describe the set of claims in a Verifiable Credential.
 */
@Serializable
data class SchemaContract(

    // Should be set to "schema".
    val id: String = SCHEMA
)