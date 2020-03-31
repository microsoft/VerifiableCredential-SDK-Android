/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.cards.Card

interface Response {

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    fun addCredential(credential: Card)

}