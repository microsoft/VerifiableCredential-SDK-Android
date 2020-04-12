package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormatterFactory @Inject constructor(private val oidcResponseFormatter: OidcResponseFormatter) {

    fun makeFormatter(request: Response): Formatter {
        when (request) {
            is OidcResponse -> return oidcResponseFormatter
            else -> throw ValidatorException("No Validator that matches Request Type.")
        }
    }
}