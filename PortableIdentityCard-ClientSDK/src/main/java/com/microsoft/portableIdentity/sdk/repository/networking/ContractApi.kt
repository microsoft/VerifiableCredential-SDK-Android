package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ContractApi {

    @GET
    fun getContract(@Url overrideUrl: String): Deferred<Response<PicContract>>
}