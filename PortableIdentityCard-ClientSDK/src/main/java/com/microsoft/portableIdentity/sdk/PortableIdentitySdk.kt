/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import android.content.Context
import com.microsoft.portableIdentity.sdk.di.DaggerSdkComponent
import com.microsoft.portableIdentity.sdk.utilities.DefaultLogConsumerBridge
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

/**
 * This class initializes the PortableIdentitySdk. The `init` method has to be called before the members can be accessed.
 * Call the init method as soon as possible, for example in the `onCreate()` method of your `Application` implementation.
 * An Android context has to be provided as such:
 *
 * PortableIdentitySdk.init(getApplicationContext());
 *
 * The `CardManager` and `IdentifierManager` can be accessed through this static reference, but ideally should be provided
 * by your own dependency injection library. In the case of Dagger2 as such:
 *
 * @Provides
 * fun provideIdentifierManager(): IdentifierManager {
 *     return PortableIdentitySdk.identifierManager
 * }
 */
object PortableIdentitySdk {

    @JvmStatic
    lateinit var cardManager: CardManager

    @JvmStatic
    lateinit var identifierManager: IdentifierManager

    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        logConsumerBridge: SdkLog.ConsumerBridge = DefaultLogConsumerBridge(),
        registrationUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
        resolverUrl: String = "http://10.91.6.163:3000"
        //resolverUrl: String = "https://dev.discover.did.msidentity.com/1.0/identifiers"
    ) {
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .build()

        identifierManager = sdkComponent.identifierManager()
        cardManager = sdkComponent.cardManager()

        SdkLog.addConsumer(logConsumerBridge)
    }
}