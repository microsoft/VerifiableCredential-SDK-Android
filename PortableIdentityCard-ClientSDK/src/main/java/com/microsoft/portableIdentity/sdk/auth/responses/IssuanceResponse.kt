// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract

class IssuanceResponse(val contract: PicContract): OidcResponse(contract.input.credentialIssuer) {

    val contractUrl: String = contract.display.contract

}