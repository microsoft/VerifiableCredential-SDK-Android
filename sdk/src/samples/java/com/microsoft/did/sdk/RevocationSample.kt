// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.util.Log
import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException

class RevocationSample {
    suspend fun revocationSample(verifiableCredential: VerifiableCredential) {
        val rpList = listOf("did:ion:12345") // provide a list of DIDs that the VC is revoked from
        when (val result = VerifiableCredentialSdk.revocationService.revokeVerifiablePresentation(verifiableCredential, rpList)) {
            is Result.Success -> handleRevokeSuccess(result.payload)
            is Result.Failure -> handleRevokeFailure(result.payload)
        }
    }

    private fun handleRevokeSuccess(revocationReceipt: RevocationReceipt) {
        // success
    }

    private fun handleRevokeFailure(sdkException: SdkException) {
        Log.e("UHOH", "something went wrong", sdkException)
    }
}