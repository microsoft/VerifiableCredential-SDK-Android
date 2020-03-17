// Copyright (c) Microsoft Corporation. All rights reserved
package com.microsoft.portableIdentity.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat

@RunWith(AndroidJUnit4ClassRunner::class)
class EllipticCurveSubtleCryptoInstrumentedTest {
    private val logger: ILogger = ConsoleLogger()
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val cryptoKeyPair: CryptoKeyPair

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, logger)
        androidSubtle = AndroidSubtle(keyStore, logger)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, logger)
        val keyReference: String = "KeyReference1"
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.Sha256,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
    }

    @Test
    fun generateKeyPairTest() {
        val expectedPrivateKeyUsage = KeyUsage.Sign
        val expectedPublicKeyUsage = KeyUsage.Verify
        assertThat(cryptoKeyPair.privateKey).isNotNull()
        assertThat(cryptoKeyPair.privateKey.type).isEqualTo(KeyType.Private)
        assertThat(cryptoKeyPair.privateKey.algorithm.name).isEqualTo(W3cCryptoApiConstants.EcDsa.value)
        assertThat(cryptoKeyPair.privateKey.usages.firstOrNull()).isEqualTo(expectedPrivateKeyUsage)
        assertThat(cryptoKeyPair.publicKey).isNotNull()
        assertThat(cryptoKeyPair.publicKey.type).isEqualTo(KeyType.Public)
        assertThat(cryptoKeyPair.publicKey.algorithm.name).isEqualTo(W3cCryptoApiConstants.EcDsa.value)
        assertThat(cryptoKeyPair.publicKey.usages.firstOrNull()).isEqualTo(expectedPublicKeyUsage)
    }

    @Test
    fun signPayloadTest() {
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        var signedPayload = ellipticCurveSubtleCrypto.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        assertThat(signedPayload).isNotNull()
    }

    @Test
    fun verifySignatureTest() {
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        var signedPayload = ellipticCurveSubtleCrypto.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        val verified = ellipticCurveSubtleCrypto.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signedPayload, payload)
        assertThat(verified).isTrue()
    }

    @Test
    fun exportPublicKeyJwk() {
        val actualKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.publicKey)
        assertThat(actualKey.kty).isEqualTo(com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.EllipticCurve.value)
        assertThat(actualKey.alg).isNotNull()
        assertThat(actualKey.alg).isEqualTo("ES256K")
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo("verify")
    }

    @Test
    fun exportPrivateKeyJwk() {
        val actualKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.privateKey)
        assertThat(actualKey.kty).isEqualTo(com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.EllipticCurve.value)
        assertThat(actualKey.alg).isNotNull()
        assertThat(actualKey.alg).isEqualTo("ES256K")
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo("sign")
    }

    @Test
    fun importPrivateKey() {
        val jsonWebKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.privateKey)
        val actualCryptoKey =
            ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, jsonWebKey, cryptoKeyPair.privateKey.algorithm, false, listOf(KeyUsage.Sign))
        assertThat(actualCryptoKey.type).isEqualTo(KeyType.Private)
    }

    @Test
    fun importPublicKey() {
        val jsonWebKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.publicKey)
        val actualCryptoKey =
            ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, jsonWebKey, cryptoKeyPair.publicKey.algorithm, false, listOf(KeyUsage.Verify))
        assertThat(actualCryptoKey.type).isEqualTo(KeyType.Public)
    }
}