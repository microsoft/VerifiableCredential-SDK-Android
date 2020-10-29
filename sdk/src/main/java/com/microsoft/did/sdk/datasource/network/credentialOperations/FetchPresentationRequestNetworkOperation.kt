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
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import kotlinx.coroutines.runBlocking
import retrofit2.Response

//TODO("override onSuccess method to create receipt when this is spec'd out")
class FetchPresentationRequestNetworkOperation(
    private val url: String,
    private val apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Serializer
) : GetNetworkOperation<String, PresentationRequestContent>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.getRequest(url) }

    override fun onSuccess(response: Response<String>): Result<PresentationRequestContent> {
        return runBlocking {
            val jwsToken = JwsToken.deserialize(response.body()!!, serializer)
            if(jwtValidator.verifySignature(jwsToken))
                Result.Success(serializer.parse(PresentationRequestContent.serializer(), jwsToken.content()))
            else
                throw InvalidSignatureException("Signature is not Valid.")
        }
    }
}