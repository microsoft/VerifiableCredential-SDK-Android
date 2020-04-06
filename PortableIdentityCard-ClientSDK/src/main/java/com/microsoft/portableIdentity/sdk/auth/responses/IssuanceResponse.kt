// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest

class IssuanceResponse(val request: IssuanceRequest): OidcResponse(request.contract.input.credentialIssuer) {

    val contractUrl: String = request.contract.display.contract

}