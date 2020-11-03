/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.di

import android.content.Context
import com.microsoft.did.sdk.LinkedDomainsService
import com.microsoft.did.sdk.IdentifierManager
import com.microsoft.did.sdk.IssuanceService
import com.microsoft.did.sdk.PresentationService
import com.microsoft.did.sdk.RevocationService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

/**
 * This interface is used by Dagger to generate the code in `DaggerSdkComponent`. It exposes the dependency graph to
 * the outside. Dagger will expose the type inferred by the return type of the interface function.
 *
 * More information:
 * https://dagger.dev/users-guide
 * https://developer.android.com/training/dependency-injection
 */
@Singleton
@Component(modules = [SdkModule::class])
internal interface SdkComponent {

    fun identifierManager(): IdentifierManager

    fun issuanceService(): IssuanceService

    fun presentationService(): PresentationService

    fun revocationService(): RevocationService

    fun linkedDomainsService(): LinkedDomainsService

    @Component.Builder
    interface Builder {
        fun build(): SdkComponent

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun resolverUrl(@Named("resolverUrl") resolverUrl: String): Builder

        @BindsInstance
        fun registrationUrl(@Named("registrationUrl") registrationUrl: String): Builder

        @BindsInstance
        fun userAgentInfo(@Named("userAgentInfo") userAgentInfo: String): Builder
    }
}