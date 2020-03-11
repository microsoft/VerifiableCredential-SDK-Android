/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.credentialRequests

/**
 * Object that represents a Verifiable Credential Request.
 */
data class VerifiableCredentialRequest(

    override val id: String,

    override  val type: CredentialRequestType = CredentialRequestType.VC,

    /**
     * A set of names of required claims from idToken.
     */
    val requiredRequestedClaims: Set<String>?,


    /**
     * A set of names of optional claims from idToken.
     */
    val optionalRequestedClaims: Set<String>?
) : CredentialRequest