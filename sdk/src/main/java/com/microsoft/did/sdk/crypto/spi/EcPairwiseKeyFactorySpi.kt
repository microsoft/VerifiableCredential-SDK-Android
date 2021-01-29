// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.KeyTypeFactory
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.util.convertToBigEndian
import com.microsoft.did.sdk.crypto.util.generatePublicKeyFromPrivateKey
import com.microsoft.did.sdk.crypto.util.publicToXY
import com.microsoft.did.sdk.crypto.util.reduceKeySeedSizeAndConvertToUnsigned
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import org.spongycastle.jce.spec.ECPrivateKeySpec
import java.lang.UnsupportedOperationException
import java.security.Key
import java.security.KeyFactorySpi
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.ECPrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class EcPairwiseKeyFactorySpi : KeyFactorySpi() {
    override fun engineGeneratePublic(keySpec: KeySpec?): PublicKey {
        throw UnsupportedOperationException("")
    }

    override fun engineGeneratePrivate(keySpec: KeySpec?): PrivateKey {
        val ecKeySpec = keySpec as? EcPairwiseKeySpec
            ?: throw InvalidKeySpecException("Keyspec has to be of type ${EcPairwiseKeySpec::class.qualifiedName}")

        val masterKey = computeHmac(ecKeySpec.userDid.toByteArray(), ecKeySpec.seed)
        val pairwiseKeySeedSigned = computeHmac(ecKeySpec.peerDid.toByteArray(), masterKey)
        val pairwiseKeySeedUnsigned = reduceKeySeedSizeAndConvertToUnsigned(pairwiseKeySeedSigned)

        val pubKey = generatePublicKeyFromPrivateKey(pairwiseKeySeedUnsigned)
        val xyData = publicToXY(pubKey)

        val pairwiseKeySeedInBigEndian = convertToBigEndian(pairwiseKeySeedUnsigned)

        ECPrivateKeySpec()

        return createPairwiseKeyFromPairwiseSeed(algorithm, pairwiseKeySeedInBigEndian, xyData)
    }

    private fun computeHmac(payload: ByteArray, seed: ByteArray): ByteArray {
        Mac.getInstance("HmacSHA512").apply {
            init(SecretKeySpec(seed, "AES"))
            doFinal(payload)
        }
    }

    private fun generatePersonaMasterKey(seedReference: String, userDid: String): ByteArray {
        // Get the seed
        val jwk = keyStore.getSecretKey(seedReference)

        keyStore.getKey<SecretKey>(seedReference)
        masterKey = generateMasterKeyFromSeed(jwk, userDid)
        masterKeys[userDid] = masterKey
        return masterKey
    }

    private fun generateMasterKeyFromSeed(jwk: KeyContainer<SecretKey>, userDid: String): ByteArray {
        // Get the subtle crypto
        val crypto: SubtleCrypto =
            subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.PRIVATE)

        // Generate the master key
        val alg = Algorithm(name = W3cCryptoApiConstants.HmacSha512.value)
        val masterJwk = JsonWebKey(
            kty = KeyType.Octets.value,
            alg = JoseConstants.Hs512.value,
            k = jwk.getKey().k
        )
        val key = crypto.importKey(
            KeyFormat.Jwk, masterJwk, alg, false, listOf(
                KeyUsage.Sign
            )
        )
        return crypto.sign(alg, key, userDid.map { it.toByte() }.toByteArray())
    }

    override fun <T : KeySpec?> engineGetKeySpec(key: Key?, keySpec: Class<T>?): T {
        throw UnsupportedOperationException("")
    }

    override fun engineTranslateKey(key: Key?): Key {
        throw UnsupportedOperationException("")
    }

}