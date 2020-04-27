/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.cards.verifiableCredential

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class VerifiableCredential(val raw: String, val contents: VerifiableCredentialContent)