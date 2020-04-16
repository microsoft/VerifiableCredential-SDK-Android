package com.microsoft.portableIdentity.sdk.repository.networking.apis

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiProvider @Inject constructor(retrofit: Retrofit) {

    val picApi: PortableIdentityCardApi = retrofit.create(PortableIdentityCardApi::class.java)
}