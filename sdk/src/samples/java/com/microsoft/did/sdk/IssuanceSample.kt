// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.util.Log
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException

class IssuanceSample {
    suspend fun issuanceSample() {
        when (val result = VerifiableCredentialSdk.issuanceService.getRequest("<issuance request url>")) {
            is Result.Success -> handleRequestSuccess(result.payload)
            is Result.Failure -> handleRequestFailure(result.payload)
        }
    }

    private suspend fun handleRequestSuccess(request: IssuanceRequest) {
        val response = IssuanceResponse(request)
        addRequestedData(response)
        when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(response)) {
            is Result.Success -> handleResponseSuccess(result.payload)
            is Result.Failure -> handleResponseFailure(result.payload)
        }
    }

    private fun handleResponseSuccess(verifiableCredential: VerifiableCredential) {
        // use verifiable credential
    }

    private fun handleRequestFailure(sdkException: SdkException) {
        Log.e("UHOH", "something went wrong", sdkException)
    }

    private fun handleResponseFailure(sdkException: SdkException) {
        Log.e("UHOH", "something went wrong", sdkException)
    }

    private fun addRequestedData(response: IssuanceResponse) {
        for (requestedClaim in response.requestedSelfAttestedClaimMap) {
            requestedClaim.setValue("your data")
        }
        for (requestedIdToken in response.requestedIdTokenMap) {
            requestedIdToken.setValue("your idToken")
        }
        for (requestedVc in response.requestedVcMap) {
//            requestedVc.setValue(yourVc)
        }
    }
}