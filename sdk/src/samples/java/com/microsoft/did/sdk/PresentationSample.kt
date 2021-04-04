// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.util.Log
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException

class PresentationSample {
    suspend fun presentationSample() {
        when (val result = VerifiableCredentialSdk.presentationService.getRequest("<presentation request url>")) {
            is Result.Success -> handleRequestSuccess(result.payload)
            is Result.Failure -> handleRequestFailure(result.payload)
        }
    }

    private suspend fun handleRequestSuccess(request: PresentationRequest) {
        val response = PresentationResponse(request)
        addRequestedData(response)
        when (val result = VerifiableCredentialSdk.presentationService.sendResponse(response)) {
            is Result.Success -> handleResponseSuccess()
            is Result.Failure -> handleResponseFailure(result.payload)
        }
    }

    private fun handleResponseSuccess() {
        // presentation was successful
    }

    private fun handleRequestFailure(sdkException: SdkException) {
        Log.e("UHOH", "something went wrong", sdkException)
    }

    private fun handleResponseFailure(sdkException: SdkException) {
        Log.e("UHOH", "something went wrong", sdkException)
    }

    private fun addRequestedData(response: PresentationResponse) {
        for (requestedVc in response.requestedVcPresentationSubmissionMap) {
//            requestedVc.setValue(yourVc) // Set values here
        }
    }
}