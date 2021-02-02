// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.util.Constants
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.Key
import java.security.KeyFactory
import java.security.KeyFactorySpi
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class EcPairwiseKeyFactorySpi : KeyFactorySpi() {
    override fun engineGeneratePublic(keySpec: KeySpec?): PublicKey {
        val ecKeySpec = keySpec as? EcPairwisePublicKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwisePublicKeySpec::class.qualifiedName}")

        val ecSpec: ECParameterSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        val q: ECPoint = ecSpec.g.multiply(ecKeySpec.privateKey.d)
        val pubSpec = ECPublicKeySpec(q, ecSpec)
        val keyFactory = KeyFactory.getInstance("EC", "SC")
        return keyFactory.generatePublic(pubSpec)
    }

    override fun engineGeneratePrivate(keySpec: KeySpec?): PrivateKey {
        val ecKeySpec = keySpec as? EcPairwisePrivateKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwisePrivateKeySpec::class.qualifiedName}")

        val masterKey = computeMac(ecKeySpec.userDid.toByteArray(), ecKeySpec.seed)
        val pairwiseKeySeedSigned = computeMac(ecKeySpec.peerDid.toByteArray(), masterKey)
        val pairwiseKeySeedUnsigned = reduceKeySeedSizeAndConvertToUnsigned(pairwiseKeySeedSigned)

        val keyFactory = KeyFactory.getInstance("EC", "SC")
        val pairwiseKeySpec = ECPrivateKeySpec(
            BigInteger(1, pairwiseKeySeedUnsigned),
            ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        )
        return keyFactory.generatePrivate(pairwiseKeySpec)
    }

    private fun computeMac(payload: ByteArray, seed: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA512").apply {
            init(SecretKeySpec(seed, "AES"))
        }
        return mac.doFinal(payload)
    }

    override fun <T : KeySpec?> engineGetKeySpec(key: Key?, keySpec: Class<T>?): T {
        throw UnsupportedOperationException("")
    }

    override fun engineTranslateKey(key: Key?): Key {
        throw UnsupportedOperationException("")
    }

    /**
     * Private key size is 32 bytes for Secp256k1. Since we use SHA512 to generate pairwise key it is 64 bytes.
     * This method computes a modulus of generated pairwise key with order N of Secp256k1 curve to reduce its size to 32 bytes. It returns BigInteger of reduced pairwise key
     * While converting BigInteger returned to byte array it is converted to signed byte array. Since we don't need the sign bit, it is truncated and converted to unsigned byte array
     * @param keySeed byte array of pairwise key generated using SHA512 (64 bytes)
     * @return unsigned byte array of pairwise key reduced to 32 bytes
     */
    private fun reduceKeySeedSizeAndConvertToUnsigned(keySeed: ByteArray): ByteArray {
        val ecSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        return convertSignedToUnsignedByteArray(BigInteger(1, keySeed).rem(ecSpec.n).toByteArray())
    }

    private fun convertSignedToUnsignedByteArray(signedByteArray: ByteArray): ByteArray {
        return when {
            signedByteArray.size > 32 -> signedByteArray.sliceArray(1 until signedByteArray.size)
            signedByteArray.size < 32 -> padByteArrayToExpectedSize(signedByteArray, 32)
            else -> signedByteArray
        }
    }

    private fun padByteArrayToExpectedSize(byteArrayToPad: ByteArray, expectedSize: Int): ByteArray {
        val paddedByteArray = ByteArray(expectedSize)
        if (byteArrayToPad.size < expectedSize) {
            byteArrayToPad.copyInto(paddedByteArray, paddedByteArray.size - byteArrayToPad.size)
        }
        return paddedByteArray
    }
}