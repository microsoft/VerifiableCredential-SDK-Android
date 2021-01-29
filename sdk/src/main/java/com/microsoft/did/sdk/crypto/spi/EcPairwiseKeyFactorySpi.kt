// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi


import com.microsoft.did.sdk.crypto.util.generatePublicKeyFromPrivateKey
import com.microsoft.did.sdk.crypto.util.publicToXY
import com.microsoft.did.sdk.crypto.util.reduceKeySeedSizeAndConvertToUnsigned
import com.microsoft.did.sdk.util.Constants
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.spec.ECPrivateKeySpec
import java.lang.UnsupportedOperationException
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
        throw UnsupportedOperationException("")
    }

    override fun engineGeneratePrivate(keySpec: KeySpec?): PrivateKey {
        val ecKeySpec = keySpec as? EcPairwiseKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwiseKeySpec::class.qualifiedName}")

        val masterKey = computeMac(ecKeySpec.userDid.toByteArray(), ecKeySpec.seed)
        val pairwiseKeySeedSigned = computeMac(ecKeySpec.peerDid.toByteArray(), masterKey)
        val pairwiseKeySeedUnsigned = reduceKeySeedSizeAndConvertToUnsigned(pairwiseKeySeedSigned)

        val pubKey = generatePublicKeyFromPrivateKey(pairwiseKeySeedUnsigned)
        val xyData = publicToXY(pubKey)
//
//        val pairwiseKeySeedInBigEndian = convertToBigEndian(pairwiseKeySeedUnsigned)
// TODO: anything needed from here?
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
}