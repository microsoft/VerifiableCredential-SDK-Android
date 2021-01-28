// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.KeyTypeFactory
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import java.lang.UnsupportedOperationException
import java.security.Key
import java.security.KeyFactorySpi
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.KeySpec
import javax.crypto.SecretKey

class EcPairwiseKeyFactorySpi : KeyFactorySpi() {
    override fun engineGeneratePublic(keySpec: KeySpec?): PublicKey {
        throw UnsupportedOperationException("")
    }

    override fun engineGeneratePrivate(keySpec: KeySpec?): PrivateKey {
        val masterKey: ByteArray = this.generatePersonaMasterKey(seedReference, userDid)

        return when (val keyType = KeyTypeFactory.createViaWebCrypto(algorithm)) {
            KeyType.EllipticCurve -> ellipticCurvePairwiseKey.generate(this, masterKey, algorithm, peerId)
            else -> throw PairwiseKeyException("Pairwise key for type '${keyType.value}' is not supported.")
        }
    }

    private fun generatePersonaMasterKey(seedReference: String, userDid: String): ByteArray {
        // Set of master keys for the different persona's
        val masterKeys: MutableMap<String, ByteArray> = mutableMapOf()

        var masterKey: ByteArray? = masterKeys[userDid]
        if (masterKey != null)
            return masterKey

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