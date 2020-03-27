/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.contracts

import com.microsoft.portableIdentity.sdk.auth.models.claimRequests.CredentialRequests
import kotlinx.serialization.Serializable

/**
 * a subset of the model in the Rules file for client consumption.
 * The input file must describe the set of inputs,
 * where to obtain the inputs and the endpoint to call to obtain a Verifiable Credential.
 */
@Serializable
data class Input (

    // Value should be set to "input".
    val id: String = "input",

    // The issuance endpoint where the request should be sent to.
    val credentialIssuer: String,

    // The DID of the issuer.
    val issuer: String,

    // Claims that are being requested.
    val attestations: CredentialRequests
)