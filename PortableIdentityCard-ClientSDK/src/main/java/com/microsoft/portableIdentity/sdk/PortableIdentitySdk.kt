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
 * The `CardManager` and `IdentityManager` can be accessed through this static reference, but ideally should be provided
 * by your own dependency injection library. In the case of Dagger2 as such:
 *
 * @Provides
 * fun provideIdentityManager(): IdentityManager {
 *     return PortableIdentitySdk.identityManager
 * }
 */
object PortableIdentitySdk {

    @JvmStatic
    lateinit var cardManager: CardManager

    @JvmStatic
    lateinit var identityManager: IdentityManager

    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        logConsumerBridge: SdkLog.ConsumerBridge = DefaultLogConsumerBridge(),
        registrationUrl: String = "https://beta.ion.microsoft.com/api/1.0/register",
        resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
        defaultSignatureKeyReference: String = "signature",
        defaultEncryptionKeyReference: String = "encryption"
    ) {
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .signatureKeyReference(defaultSignatureKeyReference)
            .encryptionKeyReference(defaultEncryptionKeyReference)
            .build()

        identityManager = sdkComponent.identityManager()
        cardManager = sdkComponent.cardManager()

        SdkLog.addConsumer(logConsumerBridge)
    }
}