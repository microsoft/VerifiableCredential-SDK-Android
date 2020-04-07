/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.attestationBindings

import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard

data class PicBinding(
        val card: PortableIdentityCard,

        val type: String
)