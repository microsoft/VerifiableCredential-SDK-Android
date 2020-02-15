// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.registrars.IRegistrar
import com.microsoft.did.sdk.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.resolvers.HttpResolver
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger

object DidSdkConfig {

    var registrationUrl: String = "https://beta.ion.microsoft.com/api/1.0/register"

    var resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers"

    var signatureKeyReference: String = "signature"

    var encryptionKeyReference: String = "encryption"

    var logger: ILogger = ConsoleLogger()

    internal lateinit var registrar: IRegistrar

    internal lateinit var resolver: IResolver

    internal lateinit var cryptoOperations: CryptoOperations

    @JvmStatic
    fun init(context: Context) {
        init(AndroidKeyStore(context, logger))
    }

    @JvmStatic
    fun init(keyStore: AndroidKeyStore) {
        val subtleCrypto = AndroidSubtle(keyStore, logger)
        val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto, logger)
        registrar = SidetreeRegistrar(registrationUrl, logger)
        resolver = HttpResolver(resolverUrl, logger)
        cryptoOperations = CryptoOperations(subtleCrypto, keyStore, logger)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }
}