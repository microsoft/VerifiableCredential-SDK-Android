/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.DidInHeaderAndPayloadNotMatching
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.controlflow.IssuanceException
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import retrofit2.Response

class FetchContractNetworkOperation(
    val url: String,
    apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : GetNetworkOperation<ContractServiceResponse, VerifiableCredentialContract>() {
    override val call: suspend () -> Response<ContractServiceResponse> = { apiProvider.issuanceApis.getContract(url) }

    override suspend fun onSuccess(response: Response<ContractServiceResponse>): Result<VerifiableCredentialContract> {
        val jwsTokenString = response.body()?.token ?: throw IssuanceException("Contract was not found in response")
        return verifyAndUnwrapContract(jwsTokenString)
    }

    private suspend fun verifyAndUnwrapContract(jwsTokenString: String): Result<VerifiableCredentialContract> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        val verifiableCredentialContract = serializer.decodeFromString(VerifiableCredentialContract.serializer(), jwsToken.content())
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Issuance Request.")
        if (!jwtValidator.validateDidInHeaderAndPayload(jwsToken, verifiableCredentialContract.input.issuer))
            throw DidInHeaderAndPayloadNotMatching("DID used to sign the contract doesn't match the DID in the contract.")
        return Result.Success(verifiableCredentialContract)
    }
}