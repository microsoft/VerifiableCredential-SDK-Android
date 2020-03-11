/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.credentialRequests

/**
 * Interface that defines a generic CredentialRequest.
 */
interface CredentialRequest {

    /**
     * id of the credentialRequest.
     */
    val id: String

    /**
     * Type of Credential Request.
     */
    val type: CredentialRequestType
}

/**
 * Enum Class that represents all Credential Request Types we support.
 */
enum class CredentialRequestType(val type: String) {
    VC("VerifiableCredential"),
    IdToken("IdToken"),
    SelfIssued("SelfIssued")
}