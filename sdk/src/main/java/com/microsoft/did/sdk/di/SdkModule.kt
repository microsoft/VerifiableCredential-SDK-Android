/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.microsoft.did.sdk.CorrelationVectorService
import com.microsoft.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.OidcPresentationRequestValidator
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.network.interceptors.CorrelationVectorInterceptor
import com.microsoft.did.sdk.datasource.network.interceptors.UserAgentInterceptor
import com.microsoft.did.sdk.identifier.registrars.Registrar
import com.microsoft.did.sdk.identifier.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.util.log.SdkLog
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * This Module is used by Dagger to provide additional types to the internal dependency graph. Usually types can be
 * provided just by annotating a class constructor with @Inject. However, this is not always possible. It's not when
 * a) Classes need special initialization outside of their constructor
 * b) Multiple instances of the same type are needed (annotate with @Named)
 * c) Interface types don't have a constructor, as such a @Provides annotation has to tell Dagger how to initialize
 *    a type that implements this interface.
 *
 * More information:
 * https://dagger.dev/users-guide
 * https://developer.android.com/training/dependency-injection
 */
@Module
class SdkModule {

    @Provides
    @Singleton
    fun defaultOkHttpClient(
        @Named("userAgentInfo") userAgentInfo: String,
        correlationVectorService: CorrelationVectorService
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor { SdkLog.d(it) }
        return OkHttpClient()
            .newBuilder()
            .followRedirects(false)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(UserAgentInterceptor(userAgentInfo))
            .addInterceptor(CorrelationVectorInterceptor(correlationVectorService))
            .build()
    }

    @Provides
    @Singleton
    fun defaultRetrofit(okHttpClient: OkHttpClient, serializer: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("http://TODO.me")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(serializer.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun defaultRegistrar(registrar: SidetreeRegistrar): Registrar {
        return registrar
    }

    @Provides
    @Singleton
    fun sdkDatabase(context: Context): SdkDatabase {
        return Room.databaseBuilder(context, SdkDatabase::class.java, "vc-sdk-db")
            .fallbackToDestructiveMigration() // TODO: Remove during public preview
            .build()
    }

    @Provides
    @Singleton
    fun defaultPresentationRequestValidator(validator: OidcPresentationRequestValidator): PresentationRequestValidator {
        return validator
    }

    @Provides
    @Singleton
    fun defaultDomainLinkageCredentialValidator(validator: JwtDomainLinkageCredentialValidator): DomainLinkageCredentialValidator {
        return validator
    }

    @Provides
    @Singleton
    fun defaultJsonSerializer(
        @Named("polymorphicJsonSerializer") additionalJsonSerializers: SerializersModule = Json.serializersModule
    ): Json {
        return Json {
            serializersModule = additionalJsonSerializers +
                SerializersModule {
                    polymorphic(UnprotectedBackupData::class) {
                        subclass(Microsoft2020UnprotectedBackupData::class)
                    }
                }
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun defaultSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}