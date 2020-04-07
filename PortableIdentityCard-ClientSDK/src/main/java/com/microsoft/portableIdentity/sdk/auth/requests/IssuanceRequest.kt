/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract

class IssuanceRequest(val contract: PicContract): Request {
    override fun getCredentialAttestations(): CredentialAttestations? {
        return contract.input.attestations
    }
}