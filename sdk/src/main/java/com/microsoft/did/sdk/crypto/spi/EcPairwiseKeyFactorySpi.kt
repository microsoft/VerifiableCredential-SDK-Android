// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.MacAlgorithm
import com.microsoft.did.sdk.crypto.PrivateKeyFactoryAlgorithm
import com.microsoft.did.sdk.crypto.PublicKeyFactoryAlgorithm
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.AES_KEY
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.Key
import java.security.KeyFactorySpi
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * This KeyFactorySpi is registered in DidProvider to provide Pairwise Key generation through the Java Security Framework.
 *
 * See class and method comments of KeyFactorySpi for more detailed information about this functions.
 *
 * A Pairwise key pair is deterministically generated given the original key and information about the peer that this key
 * is supposed to be used with. The pairwise key provides anonymity such that peers can not correlate the entity using them
 * through the signatures used for communication.
 */
class EcPairwiseKeyFactorySpi : KeyFactorySpi() {

    override fun engineGeneratePublic(keySpec: KeySpec?): PublicKey {
        val ecKeySpec = keySpec as? EcPairwisePublicKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwisePublicKeySpec::class.qualifiedName}")

        val ecSpec: ECParameterSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
        val q: ECPoint = ecSpec.g.multiply(ecKeySpec.privateKey.s)
        val qNormalized = q.normalize()
        return CryptoOperations.generateKey(
            PublicKeyFactoryAlgorithm.Secp256k1(
                qNormalized.affineXCoord.toBigInteger(),
                qNormalized.affineYCoord.toBigInteger()
            )
        )
    }

    override fun engineGeneratePrivate(keySpec: KeySpec?): PrivateKey {
        val ecKeySpec = keySpec as? EcPairwisePrivateKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwisePrivateKeySpec::class.qualifiedName}")

        val pairwiseKeySeedSigned = computeMac(ecKeySpec.peerDid.toByteArray(), ecKeySpec.personaSeed)
        val pairwiseKeySeedUnsigned = reduceKeySeedSizeAndConvertToUnsigned(pairwiseKeySeedSigned)

        return CryptoOperations.generateKey(PrivateKeyFactoryAlgorithm.Secp256k1(BigInteger(1, pairwiseKeySeedUnsigned)))
    }

    private fun computeMac(payload: ByteArray, seed: ByteArray): ByteArray {
        return CryptoOperations.computeMac(payload, SecretKeySpec(seed, AES_KEY), MacAlgorithm.HmacSha512)
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