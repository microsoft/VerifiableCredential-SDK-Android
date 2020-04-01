package com.microsoft.portableIdentity.sdk.crypto

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CryptoOperationsInstrumentedTest {
    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val keyStore: AndroidKeyStore
    private val keyRef: String = "TestKeysCryptoOperations"
    private val crypto: CryptoOperations
//    private val keyPair: CryptoKeyPair

    init {
        keyStore = AndroidKeyStore(context)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle)
        crypto = CryptoOperations(androidSubtle, keyStore)
/*        keyPair =  androidSubtle.generateKeyPair(
            RsaOaepParams(additionalParams = mapOf(Pair("KeyReference", keyRef))),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        val privateKey = RsaPrivateKey(androidSubtle.exportKeyJwk(keyPair.privateKey), logger)
        keyStore.save(keyRef, privateKey)*/
/*        keyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.Sha256,
                    "KeyReference" to keyRef
                )
            ), true, listOf(KeyUsage.Sign)
        )
        val ecPrivateKey = EllipticCurvePrivateKey(ellipticCurveSubtleCrypto.exportKeyJwk(keyPair.privateKey), logger)
        keyStore.save(keyRef, ecPrivateKey)*/
    }

    @Test
    fun generateEllipticCurveKeyPairTest() {
        val publicKey = crypto.generateKeyPair(keyRef, KeyType.EllipticCurve)
        val publicKeyJWK = publicKey.toJWK()
        assertThat(publicKeyJWK.kid).isNotNull()
        val expectedKeyType = "EC"
        val actualKeyType = publicKeyJWK.kty
        assertThat(actualKeyType).isEqualTo(expectedKeyType)
    }

    @Test
    fun generateRSAKeyPairTest() {
        val publicKey = crypto.generateKeyPair(keyRef, KeyType.RSA)
        val publicKeyJWK = publicKey.toJWK()
        assertThat(publicKeyJWK.kid).isNotNull()
        val expectedKeyType = "RSA"
        val actualKeyType = publicKeyJWK.kty
        assertThat(actualKeyType).isEqualTo(expectedKeyType)
    }

/*    @Test
    fun signAndVerifyPayloadTest() {
        val testPayload = "test string"
        val payload = testPayload.toByteArray()
        val signingKey = crypto.keyStore.getPrivateKey(keyRef).getKey()
        val algorithmName = signingKey.alg!!
        val signedPayload = crypto.sign(payload, keyRef, JwaCryptoConverter.jwaAlgToWebCrypto(algorithmName, logger = logger))

        crypto.verify(payload, signedPayload, keyRef, Algorithm(algorithmName))
    }*/
}