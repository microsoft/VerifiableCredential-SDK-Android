package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import org.bitcoin.NativeSecp256k1
import java.security.SecureRandom

class Secp256k1Provider(): Provider() {
    companion object {
        init {
            System.loadLibrary("secp256k1")
        }
    }

    override val name: String = "ECDSA"
    override val privateKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Sign)
    override val publicKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Verify)
    override val symmetricKeyUsage: Set<KeyUsage>? = null

    override fun onGenerateKeyPair(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKeyPair {
        val seed = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(seed)
        NativeSecp256k1.randomize(seed)

        val secret = ByteArray(32)
        random.nextBytes(secret)

        val publicKey = NativeSecp256k1.computePubkey(secret)

        val keyPair = CryptoKeyPair(CryptoKey(
            KeyType.Private,
            extractable,
            algorithm,
            keyUsages.toList(),
            secret
        ), CryptoKey(
            KeyType.Public,
            true,
            algorithm,
            publicKeyUsage.toList(),
            publicKey
        ))

        return return keyPair
    }

    override fun checkGenerateKeyParams(algorithm: Algorithm) {
        val keyGenParams = algorithm as? EcKeyGenParams ?: throw Error("EcKeyGenParams expected as algorithm")
        if (keyGenParams.namedCurve.toUpperCase() != W3cCryptoApiConstants.Secp256k1.value.toUpperCase() &&
            keyGenParams.namedCurve.toUpperCase() != W3cCryptoApiConstants.Secp256k1.name.toUpperCase()) {
            throw Error("The curve ${keyGenParams.namedCurve} is not supported by Secp256k1Provider")
        }
    }
}