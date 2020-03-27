// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import android.content.Context
import com.microsoft.portableIdentity.sdk.di.DaggerSdkComponent
import com.microsoft.portableIdentity.sdk.utilities.AndroidLogCatConsumer
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

object PortableIdentitySdk {

    @JvmStatic
    lateinit var cardManager: CardManager

    @JvmStatic
    lateinit var identityManager: IdentityManager

    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        logConsumer: SdkLog.Consumer = AndroidLogCatConsumer(),
        registrationUrl: String = "https://beta.ion.microsoft.com/api/1.0/register",
        resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
        signatureKeyReference: String = "signature",
        encryptionKeyReference: String = "encryption"
    ) {
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .signatureKeyReference(signatureKeyReference)
            .encryptionKeyReference(encryptionKeyReference)
            .build()

        identityManager = sdkComponent.identityManager()
        cardManager = sdkComponent.cardManager()

        SdkLog.addConsumer(logConsumer)
    }
}