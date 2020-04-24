package com.microsoft.portableIdentity.sdk.crypto.keys.rsa

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithms.RsaHashedKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import java.math.BigInteger
import java.security.spec.EllipticCurve

class RsaPairwiseKey {
    companion object {
        var numberOfPrimeTests = 0

        class PrimeDelegate(crypto: CryptoOperations, inx: Int, key: ByteArray, data: ByteArray, deterministicKey: ByteArray)
        typealias primeDelegate = List<PrimeDelegate()>
//        typealias primeDelegate = List<PrimeDelegate<CryptoOperations, Int, ByteArray, ByteArray, ByteArray>> -> ByteArray
        fun generate(crypto: CryptoOperations, personaMasterKey: ByteArray, algorithm: RsaHashedKeyGenParams, peerId: String): PrivateKey {
            val keySize = algorithm.modulusLength
            val peerIdByteArray = peerId.toByteArray()
        }

        fun generateDeterministicNumberForPrime(crypto: CryptoOperations, primeSize: Int, personaMasterKey: ByteArray, peerId: ByteArray): ByteArray {
            val numberOfRounds = primeSize / (8*64)
            val deterministicKey = "".toByteArray()
            val rounds = []
            for(inx in 0..numberOfRounds) {
                rounds.push() {
                    deterministicKey = generateHashForPrime(crypto, inx, key, data, deterministicKey)
                    return deterministicKey
                }
            }
            EllipticCurve
            return this.executeRounds(crypto, rounds, 0, personaMasterKey, peerId, deterministicKey)
        }

        fun generateHashForPrime(crypto: CryptoOperations, inx: Int, key: ByteArray, data: ByteArray, deterministicKey: ByteArray): ByteArray {
            val crypto: SubtleCrypto =
                crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);
            // Generate the master key
            val alg: Algorithm =
                EcdsaParams(
                    hash = Sha.Sha512
                )
            val signingKey = JsonWebKey(
                kty = KeyType.Octets.value,
                alg = JoseConstants.Hs512.value,
                k = Base64Url.encode(key)
            )
            val importedKey = crypto.importKey(
                KeyFormat.Jwk, signingKey, alg, false, listOf(
                    KeyUsage.Sign
                )
            )
            val signature = crypto.sign(alg, importedKey, data)
            return deterministicKey+signature
        }

        fun executeRounds(crypto: CryptoOperations, rounds: PrimeDelegate, inx: Int, key: ByteArray, data: ByteArray, deterministicKey: ByteArray): ByteArray {
            deterministicKey = rounds[inx](crypto, inx, key, data, deterministicKey)
            if(inx+1 == rounds.length)
                return deterministicKey
            else {
                deterministicKey = executeRounds(crypto, rounds, inx, key, data, deterministicKey)
                return deterministicKey
            }
        }

        private fun generatePrime(primeSeed: IntArray): BigInteger {
            primeSeed[primeSeed.size-1] = primeSeed[primeSeed.size-1].or(0x01)
            primeSeed[0] = primeSeed[0].or(0x80)
            val primeSeedBytes = primeSeed.foldIndexed(ByteArray(primeSeed.size)) { index, acc, element -> acc.apply { set(index, element.toByte()) } }
            val two = BigInteger.valueOf(2)
            //TODO: Check how to create a big integer of certain base from digits
            var prime = BigInteger(1, primeSeedBytes)
            RsaPairwiseKey.numberOfPrimeTests = 1
            while(true) {
                if(prime.isProbablePrime(64))
                    break;
                prime = prime.add(two)
                RsaPairwiseKey.numberOfPrimeTests++
            }
            return prime
        }

        fun getPrime(primeBase: ByteArray): BigInteger {
            val primeSeedArray = primeBase.foldIndexed(IntArray(primeBase.size)) { index, acc, element -> acc.apply { set(index, element.toInt()) } }
            return generatePrime(primeSeedArray)
        }
    }
}