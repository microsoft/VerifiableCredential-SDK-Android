// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest

class PresentationResponse(val request: OidcRequest): OidcResponse(request.content.clientId) {

    val nonce: String? = request.content.nonce

    val state: String? = request.content.state

    fun getRequestContents(): OidcRequestContent {
        return request.content
    }
}