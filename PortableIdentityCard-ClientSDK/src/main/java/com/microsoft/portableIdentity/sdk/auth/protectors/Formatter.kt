package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.auth.responses.ServiceRequest
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result

interface Formatter {
    fun formAndSignRequest(request: ServiceRequest, responder: Identifier, expiresIn: Int): Result<String>
}