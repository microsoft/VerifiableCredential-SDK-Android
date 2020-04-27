package com.microsoft.portableIdentity.sdk.crypto.keys.rsa

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithms.RsaHashedKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import java.math.BigInteger

typealias PrimeDelegate = MutableList<(crypto: CryptoOperations, inx: Int, key: ByteArray, data: ByteArray, deterministicKey: ByteArray) -> ByteArray>

class RsaPairwiseKey {
    companion object {
        private var numberOfPrimeTests = 0

        fun generate(crypto: CryptoOperations, personaMasterKey: ByteArray, algorithm: RsaHashedKeyGenParams, peerId: String): PrivateKey {
            val keySize = algorithm.modulusLength
            val peerIdByteArray = peerId.toByteArray()
            val pBase = generateDeterministicNumberForPrime(crypto, (keySize / 2u).toInt(), personaMasterKey, peerIdByteArray)

            val qBase = generateDeterministicNumberForPrime(crypto, (keySize / 2u).toInt(), pBase, peerIdByteArray)
            val p = getPrime(pBase)
            val q = getPrime(qBase)

            val modulus = p.multiply(q)
            val pMinus = p.subtract(BigInteger.ONE)
            val qMinus = q.subtract(BigInteger.ONE)
            val phi = pMinus.multiply(qMinus)
            val e = BigInteger.valueOf(65537)
            val d = e.modInverse(phi)
            val dp = d.mod(pMinus)
            val dq = d.mod(qMinus)
            val qi = q.modInverse(p)
            return RsaPrivateKey(
                JsonWebKey(
                    kty = KeyType.RSA.value,
                    use = JwaCryptoConverter.webCryptoToJwa(algorithm),
                    e = toBase(e),
                    n = toBase(modulus),
                    d = toBase(d),
                    p = toBase(p),
                    q = toBase(q),
                    dp = toBase(dp),
                    dq = toBase(dq),
                    qi = toBase(qi),
                    kid = "#kid1"
                )
            )
        }

        private fun generateDeterministicNumberForPrime(
            crypto: CryptoOperations,
            primeSize: Int,
            personaMasterKey: ByteArray,
            peerId: ByteArray
        ): ByteArray {
            val numberOfRounds = primeSize / (8 * 64)
            var deterministicKey = "".toByteArray()
            val rounds: PrimeDelegate? = null
            for (inx in 0..numberOfRounds) {
                val pd: (CryptoOperations, Int, ByteArray, ByteArray, ByteArray) -> ByteArray =
                    fun(crypto, inx, key, data, deterministicKey): ByteArray {
                        return generateHashForPrime(crypto, inx, key, data, deterministicKey)
                    }
                rounds?.add(pd)
            }
            return this.executeRounds(crypto, rounds, 0, personaMasterKey, peerId, deterministicKey)
        }

        private fun generateHashForPrime(
            crypto: CryptoOperations,
            inx: Int,
            key: ByteArray,
            data: ByteArray,
            deterministicKey: ByteArray
        ): ByteArray {
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
            return deterministicKey + signature
        }

        private fun executeRounds(
            crypto: CryptoOperations,
            rounds: PrimeDelegate?,
            inx: Int,
            key: ByteArray,
            data: ByteArray,
            deterministicKey: ByteArray
        ): ByteArray {
            var deterministicKey = rounds!![inx](crypto, inx, key, data, deterministicKey)
            return if (inx + 1 == rounds?.size)
                deterministicKey
            else {
                deterministicKey = executeRounds(crypto, rounds, inx, key, data, deterministicKey)
                deterministicKey
            }
        }

        private fun generatePrime(primeSeed: IntArray): BigInteger {
            primeSeed[primeSeed.size - 1] = primeSeed[primeSeed.size - 1].or(0x01)
            primeSeed[0] = primeSeed[0].or(0x80)
            val primeSeedBytes =
                primeSeed.foldIndexed(ByteArray(primeSeed.size)) { index, acc, element -> acc.apply { set(index, element.toByte()) } }
            val two = BigInteger.valueOf(2)
            //TODO: Check how to create a big integer of certain base from digits
            var prime = BigInteger(1, primeSeedBytes)
            numberOfPrimeTests = 1
            while (true) {
                if (prime.isProbablePrime(64))
                    break;
                prime = prime.add(two)
                numberOfPrimeTests++
            }
            return prime
        }

        private fun getPrime(primeBase: ByteArray): BigInteger {
            val primeSeedArray =
                primeBase.foldIndexed(IntArray(primeBase.size)) { index, acc, element -> acc.apply { set(index, element.toInt()) } }
            return generatePrime(primeSeedArray)
        }

        private fun toBase(bigInteger: BigInteger): String {
            val base64 = bigInteger.toString(256).toByteArray()
            return Base64Url.encode(base64)
        }
    }
}