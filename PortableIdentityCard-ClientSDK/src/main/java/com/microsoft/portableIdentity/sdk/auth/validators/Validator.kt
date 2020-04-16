package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result

interface Validator {

    suspend fun validate(request: Request): Result<Boolean>
}