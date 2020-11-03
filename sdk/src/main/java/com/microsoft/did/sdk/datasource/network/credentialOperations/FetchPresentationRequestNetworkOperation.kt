/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import retrofit2.Response

//TODO("override onSuccess method to create receipt when this is spec'd out")
class FetchPresentationRequestNetworkOperation(
    private val url: String,
    private val apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Serializer
) : GetNetworkOperation<String, PresentationRequestContent>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.getRequest(url) }

    override suspend fun onSuccess(response: Response<String>): Result<PresentationRequestContent> {
        val jwsTokenString = response.body() ?: throw PresentationException("No Presentation Request in Body.")
        return verifyAndUnwrapPresentationRequest(jwsTokenString)
    }

    private suspend fun verifyAndUnwrapPresentationRequest(jwsTokenString: String): Result<PresentationRequestContent> {
        val jwsToken = JwsToken.deserialize(jwsTokenString, serializer)
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Presentation Request.")
        return Result.Success(serializer.parse(PresentationRequestContent.serializer(), jwsToken.content()))
    }
}