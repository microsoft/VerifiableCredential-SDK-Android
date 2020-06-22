/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.di.DaggerSdkComponent
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.log.DefaultLogConsumer
import com.microsoft.did.sdk.util.log.SdkLog
import org.bitcoin.Secp256k1Context

/**
 * This class initializes the VerifiableCredentialSdk. The `init` method has to be called before the members can be accessed.
 * Call the init method as soon as possible, for example in the `onCreate()` method of your `Application` implementation.
 * An Android context has to be provided as such:
 *
 * VerifiableCredentialSdk.init(getApplicationContext());
 *
 * The `VerifiableCredentialManager` and `IdentifierManager` can be accessed through this static reference, but ideally should be provided
 * by your own dependency injection library. In the case of Dagger2 as such:
 *
 * @Provides
 * fun provideIdentifierManager(): IdentifierManager {
 *     return VerifiableCredentialSdk.identifierManager
 * }
 */
object VerifiableCredentialSdk {

    @JvmStatic
    lateinit var verifiableCredentialManager: VerifiableCredentialManager

    @JvmStatic
    lateinit var identifierManager: IdentifierManager

    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        logConsumer: SdkLog.Consumer = DefaultLogConsumer(),
        registrationUrl: String = "",
        resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
        encryptedSharedPreferencesFileName: String = Constants.SECRET_SHARED_PREFERENCES
    ) {
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .encryptedSharedPreferencesFileName(encryptedSharedPreferencesFileName)
            .build()

        identifierManager = sdkComponent.identifierManager()
        verifiableCredentialManager = sdkComponent.verifiableCredentialManager()

        SdkLog.addConsumer(logConsumer)
    }

    fun isDeviceModelSupported(): Boolean {
        return Secp256k1Context.isEnabled()
    }
}