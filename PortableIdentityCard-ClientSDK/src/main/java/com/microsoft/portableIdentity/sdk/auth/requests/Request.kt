/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations

interface Request {

    /**
     * Get Credential Requests if there are any in Request.
     *
     * @return credentials requests if exist, null if no credentials requested.
     */
    fun getCredentialAttestations(): CredentialAttestations?
}