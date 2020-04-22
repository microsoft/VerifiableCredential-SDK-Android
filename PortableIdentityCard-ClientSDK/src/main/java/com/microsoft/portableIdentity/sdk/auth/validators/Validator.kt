package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.requests.CredentialRequest
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result

interface Validator {

    suspend fun validate(request: CredentialRequest): Result<Boolean>
}