// Copyright (c) Microsoft Corporation. All rights reserved
package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.spec.ECNamedCurveSpec
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve


@RunWith(AndroidJUnit4ClassRunner::class)
class ECBCInstrumentedTest {
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private lateinit var cryptoKeyPair: CryptoKeyPair

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        val keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
    }

    @Test
    fun generateKeyPairTest() {
        val keyReference = "KeyReference1"
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
        val private = cryptoKeyPair.privateKey
        val public = cryptoKeyPair.publicKey

        val privateJwk = ellipticCurveSubtleCrypto.exportKeyJwk(private)
        val publicJwk = ellipticCurveSubtleCrypto.exportKeyJwk(public)

        assertThat(privateJwk.x).isEqualTo(publicJwk.x)
        assertThat(privateJwk.y).isEqualTo(publicJwk.y)

        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val privateKey = ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, privateJwk, alg, true, listOf(KeyUsage.Sign))
        val publicKey = ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, publicJwk, alg, true, listOf(KeyUsage.Verify))

        assertThat(private.handle).isEqualToComparingFieldByFieldRecursively(privateKey.handle)
        assertThat(public.handle).isEqualToComparingFieldByFieldRecursively(publicKey.handle)

        val generatedPrivateKey = generatePrivateKeyFromCryptoKey(privateKey)
        assertThat((generatedPrivateKey as BCECPrivateKey).d.toByteArray()).isEqualTo((private.handle as Secp256k1Provider.Secp256k1Handle).data)

        val generatedPublicKey = generatePublicKeyFromCryptoKey(publicKey)
        assertThat((generatedPublicKey as BCECPublicKey).q.xCoord.encoded).isEqualTo(
            (public.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(
                0..31
            )
        )
        assertThat(generatedPublicKey.q.yCoord.encoded).isEqualTo((public.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63))

        val testData = "test message"
        val signature = ellipticCurveSubtleCrypto.sign(alg, privateKey, stringToByteArray(testData))

        val verified = ellipticCurveSubtleCrypto.verify(alg, publicKey, signature, stringToByteArray(testData))
        assertThat(verified).isTrue()
    }

    private fun generatePrivateKeyFromCryptoKey(key: CryptoKey): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec =
            java.security.spec.ECPrivateKeySpec(BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePublicKeyFromCryptoKey(key: CryptoKey): PublicKey {
/*        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: java.security.spec.ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val publicKeyJwk = ellipticCurveSubtleCrypto.exportKeyJwk(key)
        val publicKeySpec =
            java.security.spec.ECPublicKeySpec(
                ECPoint(
                    BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(0..31)),
                    BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63))
                ),
                curveSpec
            )
        return keyFactory.generatePublic(publicKeySpec)*/

        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val curve = params.curve
        val ellipticCurve: EllipticCurve = EC5Util.convertCurve(curve, params.seed)
        val x = (key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(0..31)
        val y = (key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63)
        val encoded = byteArrayOf(0x04) + x + y
        val point: ECPoint = ECPointUtil.decodePoint(ellipticCurve, encoded)
        val params2: ECParameterSpec = EC5Util.convertSpec(ellipticCurve, params)
        val keySpec = ECPublicKeySpec(point, params2)
        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    @Test
    fun nativeSignAndSCVerifyTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        var verified = false
        var testData = "this is a random test message for testing signing and verifying"
        for (i in 0..10) {
            val keyReference = "KeyReference$i"
            cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
                EcKeyGenParams(
                    namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                    additionalParams = mapOf(
                        "hash" to Sha.SHA256.algorithm,
                        "KeyReference" to keyReference
                    )
                ), true, listOf(KeyUsage.Sign)
            )
            val private = cryptoKeyPair.privateKey
            val public = cryptoKeyPair.publicKey
            testData += i
            val signature = ellipticCurveSubtleCrypto.onNativeSign(alg, private, stringToByteArray(testData))

            verified = ellipticCurveSubtleCrypto.verify(alg, public, signature, stringToByteArray(testData))
            if (verified)
                break
        }
        assertThat(verified).isTrue()
    }

    @Test
    fun scSignAndNativeVerifyTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        var verified = false
        var testData = "this is a random test message for testing signing and verifying"
        for (i in 0..10) {
            val keyReference = "KeyReference$i"
            cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
                EcKeyGenParams(
                    namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                    additionalParams = mapOf(
                        "hash" to Sha.SHA256.algorithm,
                        "KeyReference" to keyReference
                    )
                ), true, listOf(KeyUsage.Sign)
            )
            val private = cryptoKeyPair.privateKey
            val public = cryptoKeyPair.publicKey
            testData += i
            var signature = ellipticCurveSubtleCrypto.sign(alg, private, stringToByteArray(testData))
            val rs = decodeFromDER(signature)
            val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
            ecParams.n.shiftRight(1)
            if(rs.second.compareTo(ecParams.getN().shiftRight(1)) > 0) {
                val canonicalized = canonicalize(rs.first, rs.second)
                signature = encodeToDer(canonicalized.first.toByteArray(), canonicalized.second.toByteArray())
            }
            verified = ellipticCurveSubtleCrypto.nativeVerify(alg, public, signature, stringToByteArray(testData))
            if (verified)
                break
        }
        assertThat(verified).isTrue()
    }

    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    fun canonicalize(r: BigInteger, s: BigInteger): Pair<BigInteger, BigInteger> {
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        return Pair(r, ecDomainParameters.n.subtract(s))
    }
    fun decodeFromDER(bytes: ByteArray): Pair<BigInteger, BigInteger> {
        val decoder = ASN1InputStream(bytes)
        try {
            val seq = decoder.readObject() as DLSequence
            val r = seq.getObjectAt(0) as ASN1Integer
            val s = seq.getObjectAt(1) as ASN1Integer
            return Pair(r.positiveValue, s.positiveValue)
        } catch (e: IOException) {
            throw RuntimeException(e);
        } finally {
            if (decoder != null)
                try {
                    decoder.close()
                } catch (x: IOException) {
                }
        }
    }
}