/*
package com.microsoft.portableIdentity.sdk

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat

@RunWith(AndroidJUnit4ClassRunner::class)
class IdentityManagerInstrumentedTest {
    private val signatureKeyReference: String
    private val encryptionKeyReference: String
    private val recoveryKeyReference: String
    private val registrationUrl: String
    private val resolverUrl: String
    private val identityManager: IdentityManager
    private val androidSubtle: SubtleCrypto
    private val ecSubtle: EllipticCurveSubtleCrypto
    private val cryptoOperations: CryptoOperations
    private val didSdkConfig: DidSdkConfig

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        registrationUrl = "http://10.91.6.163:3000"
        resolverUrl = "http://10.91.6.163:3000"
        signatureKeyReference = "signature"
        encryptionKeyReference = "encryption"
        recoveryKeyReference = "recovery"
        didSdkConfig = DidSdkConfig(context, signatureKeyReference, encryptionKeyReference, recoveryKeyReference, registrationUrl, resolverUrl)
        identityManager = IdentityManager(didSdkConfig)
        val keyStore = AndroidKeyStore(context)
        androidSubtle = AndroidSubtle(keyStore)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle)
        cryptoOperations = CryptoOperations(androidSubtle, keyStore)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

    @Test
    fun createIdentifierTest() {
        assertThat(identityManager.did).isNotNull
    }

    @Test
    fun signAndVerifyTest() {
        val test = "test string"
        val testPayload = test.toByteArray()
        val token = JwsToken(testPayload)
        token.sign(identityManager.did.signatureKeyReference, cryptoOperations)
        val matched = token.verify(cryptoOperations)
        assertThat(matched).isTrue()
    }
}*/
