/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts

import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import kotlinx.serialization.Serializable

/**
 * A logical grouping of documents created by an issuer to enable the creation of a Verifiable Credential and meta-data.
 * In the Verifiable Credential Service, there are four files that make up a contract:
 * schema, display, and input.
 */
@Serializable
data class VerifiableCredentialContract(
    // unique identifier of the contract
    val id: String,

    // A subset of the model in the Rules file for client consumption. The input file must describe the set of inputs,
    // where to obtain the inputs and the endpoint to call to obtain a Verifiable Credential.
    val input: InputContract,

    // A user experience data file that describes how information in a Verifiable Credential may be displayed.
    val display: DisplayContract
)