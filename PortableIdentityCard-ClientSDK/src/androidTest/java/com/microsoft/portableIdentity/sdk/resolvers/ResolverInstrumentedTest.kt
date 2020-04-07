// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.resolvers

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ResolverInstrumentedTest {
    private val resolver: IResolver
    private val crypto: CryptoOperations
    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val keyStore: AndroidKeyStore
    init {
        keyStore = AndroidKeyStore(context)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle)
        crypto = CryptoOperations(androidSubtle, keyStore)
        resolver = HttpResolver("https://beta.discover.did.microsoft.com/1.0/identifiers")
    }

    @Test
    fun resolveTest() {
        var resolvedIdentifierDocument: IdentifierDocument
        runBlocking {
            val identifier = resolver.resolve("did:ion:test:EiCAvQuaAu5awq_e_hXyJImdQ5-xJsZzzQ3Xd9a2EAphtQ", crypto)
            resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isNotNull()
        }

    }
}