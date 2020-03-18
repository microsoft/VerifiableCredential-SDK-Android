/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.credentialRequests

import kotlinx.serialization.Serializable

/**
 * Interface that defines a generic CredentialRequest.
 */

data class CredentialRequest<T>(val claims: Map<String, T>)

/**
 * Enum Class that represents all Credential Request Types we support.
 */
enum class CredentialRequestType(val type: String) {
    VC("VerifiableCredential"),
    IdToken("IdToken"),
    SelfIssued("SelfIssued")
}