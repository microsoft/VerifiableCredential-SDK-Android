/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract

class IssuanceRequest(val contract: PicContract, val contractUrl: String): Request {

    override fun getCredentialAttestations(): CredentialAttestations? {
        return contract.input.attestations
    }

    // Private Preview: gets first contract from each Verifiable Credential Attestation.
    override fun getContractUrls(): List<String> {
        val attestations = contract.input.attestations ?: return emptyList()
        val contracts = mutableListOf<String>()
        attestations.presentations.forEach {
            contracts.add(it.contracts.first())
        }
        return contracts
    }
}