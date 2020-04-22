// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.CredentialRequest

class PresentationResponse(val request: CredentialRequest.PresentationRequest): OidcResponse(request.contents.clientId) {

    val nonce: String? = request.contents.nonce

    val state: String? = request.contents.state

    fun getRequestContents(): OidcRequestContent {
        return request.contents
    }
}