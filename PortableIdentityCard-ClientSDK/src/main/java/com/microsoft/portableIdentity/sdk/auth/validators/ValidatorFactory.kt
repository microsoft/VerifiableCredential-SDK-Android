package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidatorFactory @Inject constructor(private val oidcRequestValidator: OidcRequestValidator) {

    fun makeValidator(request: Request): Validator {
        when (request) {
            is OidcRequest -> return oidcRequestValidator
            else -> throw ValidatorException("No Validator that matches Request Type.")
        }
    }
}