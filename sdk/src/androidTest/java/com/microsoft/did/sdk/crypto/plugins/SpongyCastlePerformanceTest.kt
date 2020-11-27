// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.convertSignedToUnsignedByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Security

class SpongyCastlePerformanceTest {
    private val testName = "SpongyCastlePerformanceTest"

    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
    }

    private fun generatePrivateKey(): ByteArray {
        val random = SecureRandom()
        val secret = ByteArray(32)
        random.nextBytes(secret)
        return secret
    }

    private fun createCurveParameters(): ECDomainParameters {
        val ecParams = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        return ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
    }

    fun getTestName(): String {
        return this.testName
    }

    fun getStartTime(): Long {
        return System.nanoTime()
    }

    fun timer(start: Long): String {
        val timing = System.nanoTime() - start
        return (timing/1000).toString()
    }


    @Test
    fun signAndVerifyPerformanceTest() {
        val privateKey = generatePrivateKey()
        val publicKey = generatePublicKeyFromPrivateKey(privateKey)
        val data = ByteArray(2048) {it.toByte()}

        for (loop in 0..9) {
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): Start hash: 0")
            var startTime = getStartTime()

            val digest = MessageDigest.getInstance(Sha.SHA256.algorithm.name)
            val hash = digest.digest(data)
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): End hash: ${timer(startTime)}")

            println("PerfTest->(${getTestName()}) in  μs - (${loop}): Start sign: 0")
            startTime = getStartTime()
            val signature = signPayload(hash, privateKey)
            val r = convertSignedToUnsignedByteArray(signature[0].toByteArray())
            val s = convertSignedToUnsignedByteArray(signature[1].toByteArray())
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): End sign: ${timer(startTime)}")

            println("PerfTest->(${getTestName()}) in  μs - (${loop}): Start verify: 0")
            startTime = getStartTime()
            verifySignature(r + s, hash, publicKey)
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): End verify: ${timer(startTime)}")
        }
    }

    private fun signPayload(hashedData: ByteArray, keyData: ByteArray): Array<out BigInteger> {
        val signingSigner = ECDSASigner()
        val ecDomainParameters = createCurveParameters()
        val privateKeyParams = ECPrivateKeyParameters(BigInteger(1, keyData), ecDomainParameters)
        signingSigner.init(true, privateKeyParams)

        return signingSigner.generateSignature(hashedData)
    }

    private fun verifySignature(signature: ByteArray, hashedData: ByteArray, keyData: ByteArray): Boolean {
        val verifySigner = ECDSASigner()
        val ecDomainParameters = createCurveParameters()
        val publicKeyParams = ECPublicKeyParameters(ecDomainParameters.curve.decodePoint(keyData), ecDomainParameters)
        verifySigner.init(false, publicKeyParams)

        return verifySigner.verifySignature(
            hashedData,
            BigInteger(1, signature.sliceArray(0 until signature.size / 2)),
            BigInteger(1, signature.sliceArray(signature.size / 2 until signature.size))
        )
    }

    private fun generatePublicKeyFromPrivateKey(privateKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val ecSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        val ecPoint = ecSpec.g.multiply(BigInteger(1, privateKey))
        val pubKeySpec = ECPublicKeySpec(ecPoint, ecSpec)
        val publicKey = keyFactory.generatePublic(pubKeySpec)
        return byteArrayOf(Secp256k1Provider.Secp256k1Tag.UNCOMPRESSED.byte) + (publicKey as BCECPublicKey).q.normalize().xCoord.encoded +
            publicKey.q.normalize().yCoord.encoded
    }
}