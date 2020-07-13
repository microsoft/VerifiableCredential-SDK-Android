/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.requestMappings

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

data class VerifiableCredentialHolderRequestMapping(
    val verifiablePresentationHolder: VerifiableCredentialHolder,
    val presentationAttestation: PresentationAttestation
)