/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.protectors.IProtector
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer
import java.lang.Exception

class Response(val request: Request?) {

    val collectedCredentials: MutableSet<Credential> = mutableSetOf()

    var signer: Signer? = null

    fun addCredential(credential: Credential) {
        collectedCredentials.add(credential)
    }

    fun addProtector(protector: IProtector) {
        if (protector is Signer) {
            signer = protector
        } else {
            throw Exception("We do not support Encryption at the moment.")
        }
    }

    /**
     * 1. Composes ResponseContents from RequestContents and collected credentials.
     * 2. Protects contents with protectors if exist.
     * 3. Sends Response to url.
     */
    fun send(url: String) {

    }
}