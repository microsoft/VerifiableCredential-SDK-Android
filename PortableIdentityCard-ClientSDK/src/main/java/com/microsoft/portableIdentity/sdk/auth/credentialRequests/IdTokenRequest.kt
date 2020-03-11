/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.auth.credentialRequests

data class IdTokenRequest(

    /**
     * should be the OIDC endpoint.
     */
    override val id: String,

    /**
     * always set to IdToken
     */
    override val type: CredentialRequestType = CredentialRequestType.IdToken,

    /**
     * A set of names of required claims from idToken.
     */
    val requiredRequestedClaims: Set<String>?,


    /**
     * A set of names of optional claims from idToken.
     */
    val optionalRequestedClaims: Set<String>?
): CredentialRequest