/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.credentialRequests

/**
 * Object that bundles types of credential requests together if exists.
 */
data class CredentialRequests(
    val selfIssuedCredentialRequests: Set<SelfIssuedCredentialRequest>?,
    val verifiableCredentialRequests: Set<VerifiableCredentialRequest>?,
    val idTokenRequests: Set<IdTokenRequest>?) {
}