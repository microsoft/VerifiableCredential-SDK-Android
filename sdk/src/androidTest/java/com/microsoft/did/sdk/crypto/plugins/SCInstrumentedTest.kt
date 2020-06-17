// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
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
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.crypto.generators.ECKeyPairGenerator
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECKeyGenerationParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Security

@RunWith(AndroidJUnit4ClassRunner::class)
class SCInstrumentedTest {
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
    fun scTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val algorithm = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm,
                "KeyReference" to "keyReference"
            )
        )
        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        //generate key pair
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val keyGen = ECKeyPairGenerator()
        val random = SecureRandom()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val gParam = ECKeyGenerationParameters(ecDomainParameters, random)
        keyGen.init(gParam)
        val keyPair = keyGen.generateKeyPair()

        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", (keyPair.public as ECPublicKeyParameters).q.getEncoded(false))
        )

        val privateKey = CryptoKey(
            KeyType.Private,
            true,
            signAlgorithm,
            listOf(KeyUsage.Sign),
            Secp256k1Provider.Secp256k1Handle("", (keyPair.private as ECPrivateKeyParameters).d.toByteArray())
        )

        //Sign using SC
        val payload = stringToByteArray("testing")
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(payload)
        val signingSigner = ECDSASigner()
        val privateKeyParams = ECPrivateKeyParameters((keyPair.private as ECPrivateKeyParameters).d, ecDomainParameters)
        signingSigner.init(true, privateKeyParams)
        val components = signingSigner.generateSignature(hashed)
        val signature = encodeToDer(components[0].toByteArray(), components[1].toByteArray())

        val bigintfromarray: BigInteger = BigInteger((privateKey.handle as Secp256k1Provider.Secp256k1Handle).data)
        val bigInt = (keyPair.private as ECPrivateKeyParameters).d
        assertThat(bigInt).isEqualTo(bigintfromarray)

        val signingSigner2 = ECDSASigner()
        val privateKeyParams2 = ECPrivateKeyParameters(BigInteger((privateKey.handle as Secp256k1Provider.Secp256k1Handle).data), ecDomainParameters)
        assertThat(privateKeyParams).isEqualToComparingFieldByFieldRecursively(privateKeyParams2)
        signingSigner2.init(true, privateKeyParams2)
        val components2 = signingSigner2.generateSignature(hashed)
        val signature2 = encodeToDer(components2[0].toByteArray(), components2[1].toByteArray())

        val sign = ellipticCurveSubtleCrypto.sign(alg, privateKey, hashed)
//        assertThat(sign).isEqualTo(signature)
        assertThat(components[0]).isEqualTo(components2[0])
        assertThat(components[1]).isEqualTo(components2[1])
        assertThat(sign).isEqualTo(signature2)

        //Verify using SC
        val verifySigner = ECDSASigner()
        val params = ECPublicKeyParameters((keyPair.public as ECPublicKeyParameters).q, ecDomainParameters)
        verifySigner.init(false, params)
        val verified = verifySigner.verifySignature(hashed, components[0], components[1])
        assertThat(verified).isTrue()

        val verify = ellipticCurveSubtleCrypto.verify(alg, publicKey, signature, hashed)
        assertThat(verify).isTrue()

        //Verify using Native lib
        val verifyAgain = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey,signature, hashed)
        assertThat(verifyAgain).isTrue()
    }

    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    @Test
    fun bigint() {
        val d = BigInteger("99128494378857014826154783673634777911767718767766215122340613264700837252865")
        val darray = d.toByteArray()
        val bagain = BigInteger(darray)
    }
}