// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.repository.VerifiableCredentialRepository
import com.microsoft.portableIdentity.sdk.repository.networking.ContractApi
import com.microsoft.portableIdentity.sdk.repository.networking.ContractRepository
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper
import io.ktor.http.Url
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardManager @Inject constructor(
    private val vcRepository: VerifiableCredentialRepository,
    private val retrofit: Retrofit,
    private val cryptoOperations: CryptoOperations,
    private val resolver: IResolver,
    private val validator: OidcRequestValidator // TODO: should this be a generic Validator?
) {

    /**
     * Get contract from PICS.
     */
    suspend fun getContract(url: String): PicContract? {
        val contractApi : ContractApi = retrofit.create(ContractApi::class.java)
        val repository : ContractRepository = ContractRepository(contractApi)
        val contract = repository.getContract(url)
        print(contract)
        return contract
    }

    /**
     * Create a Request Object from a uri.
     */
    suspend fun getRequest(uri: String): Request {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            throw Exception("request format not supported")
        }

        val requestParameters = url.parameters.toMap()
        val serializedToken = requestParameters["request"]?.first()
        if (serializedToken != null) {
            return OidcRequest(requestParameters, serializedToken)
        }

        val requestUri = requestParameters["request_uri"]?.first()
        val requestToken = HttpWrapper.get(requestUri!!)
        return OidcRequest(requestParameters, requestToken)
    }

    @Deprecated("Old OidcRequest for old POC. Remove when new Model is up.")
    suspend fun parseOidcRequest(request: String): com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcRequest {
        return withContext(Dispatchers.IO) {
            com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcRequest.parseAndVerify(request, cryptoOperations, resolver)
        }
    }

    /**
     * Validate an OpenID Connect Request.
     */
    suspend fun validate(request: OidcRequest) {
        validator.validate(request)
    }

    /**
     * Send a Response
     */
    suspend fun send(response: OidcResponse) {
        TODO("use retrofit for API calls")
    }

    /**
     *
     */
    suspend fun saveClaim(claim: ClaimObject) {
        vcRepository.insert(claim)
    }

    fun getClaims(): LiveData<List<ClaimObject>> {
        return vcRepository.getAllClaimObjects()
    }
}