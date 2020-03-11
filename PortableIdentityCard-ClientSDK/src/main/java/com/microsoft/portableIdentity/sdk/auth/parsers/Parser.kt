/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests

/**
 * Interface for defining methods on a Parser Object that parses raw requests.
 */
interface Parser {

    fun getCredentialRequests(): CredentialRequests
}