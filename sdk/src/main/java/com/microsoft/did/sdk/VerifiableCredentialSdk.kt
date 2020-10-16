/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.di.DaggerSdkComponent
import com.microsoft.did.sdk.util.log.DefaultLogConsumer
import com.microsoft.did.sdk.util.log.SdkLog

/**
 * This class initializes the VerifiableCredentialSdk. The `init` method has to be called before the members can be accessed.
 * Call the init method as soon as possible, for example in the `onCreate()` method of your `Application` implementation.
 * An Android context has to be provided as such:
 *
 * VerifiableCredentialSdk.init(getApplicationContext());
 *
 * The `VerifiableCredentialManager` can be accessed through this static reference, but ideally should be provided
 * by your own dependency injection library. In the case of Dagger2 as such:
 *
 * @Provides
 * fun provideIdentifierManager(): IdentifierManager {
 *     return VerifiableCredentialSdk.identifierManager
 * }
 */
object VerifiableCredentialSdk {

    @JvmStatic
    lateinit var issuanceService: IssuanceService

    @JvmStatic
    lateinit var presentationService: PresentationService

    @JvmStatic
    lateinit var revocationService: RevocationService

    @JvmStatic
    internal lateinit var identifierManager: IdentifierManager

    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        logConsumer: SdkLog.Consumer = DefaultLogConsumer(),
        userAgentInfo: String,
        registrationUrl: String = "",
        resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers"
    ) {
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .userAgentInfo(userAgentInfo)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .build()

        issuanceService = sdkComponent.issuanceService()
        presentationService = sdkComponent.presentationService()
        revocationService = sdkComponent.revocationService()
        identifierManager = sdkComponent.identifierManager()

        SdkLog.addConsumer(logConsumer)
    }
}