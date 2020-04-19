package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result

interface Formatter {
    fun formAndSignResponse(response: Response, responder: Identifier, expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): Result<String>
}