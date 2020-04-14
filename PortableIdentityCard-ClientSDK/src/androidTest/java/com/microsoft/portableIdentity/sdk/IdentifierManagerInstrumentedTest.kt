package com.microsoft.portableIdentity.sdk

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.registrars.SidetreeRegistrar
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class IdentifierManagerInstrumentedTest {
    private val registrar: Registrar
    private val androidSubtle: SubtleCrypto
    private val ecSubtle: EllipticCurveSubtleCrypto
    private val cryptoOperations: CryptoOperations
    private val keyStore: KeyStore

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        PortableIdentitySdk.init(context)
        registrar = SidetreeRegistrar("http://10.91.6.163:3000")
        keyStore = AndroidKeyStore(context)
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
        assertThat(PortableIdentitySdk.identifierManager.did).isNotNull
    }

    @Test
    fun signAndVerifyTest() {
        val test = "test string"
        val testPayload = test.toByteArray()
        val token = JwsToken(testPayload)
        token.sign(PortableIdentitySdk.identifierManager.did.signatureKeyReference, cryptoOperations)
        val publicKeys = keyStore.getPublicKey(PortableIdentitySdk.identifierManager.did.signatureKeyReference).keys
/*        val publicKeys = PortableIdentitySdk.identifierManager.did.document.publicKey.map {
            it.toPublicKey()
        }*/
        val matched = token.verify(cryptoOperations, publicKeys)
        assertThat(matched).isTrue()
    }
}
