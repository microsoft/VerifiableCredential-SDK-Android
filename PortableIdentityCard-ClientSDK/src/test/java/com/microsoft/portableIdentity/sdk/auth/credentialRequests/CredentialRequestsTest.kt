package com.microsoft.portableIdentity.sdk.auth.credentialRequests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.repository.networking.ContractApi
import com.microsoft.portableIdentity.sdk.repository.networking.ContractRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.Test

class CredentialRequestsTest {

    @Test
    fun testSerializer() {

        fun retrofit() : Retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()


        val contractApi : ContractApi = retrofit().create(ContractApi::class.java)
        val repository : ContractRepository = ContractRepository(contractApi)

        runBlocking {
            val contract = repository.getContract("https://portableidentitycards.azure-api.net/76B0B89D-F537-4D7D-B0BA-431B9E1CF9E2/api/portable/v1.0/contracts/EverythingBagel")
            print(contract)
        }
    }
}