import android.util.Base64
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.spec.*
import java.util.*

class AndroidSubtle: SubtleCrypto {
    override fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        // verify we're signing with a private key
        if (!key.type != KeyType.Private) {
            throw Error("Sign must use a private key")
        }
        // key's handle should be an Android keyStore key reference.
        if (!AndroidKeyStore.keyStore.containsAlias(key.handle as? String ?:
            throw Error("Non-Android cryptoKey passed"))) {
            throw Error("No key found for cryptoKey ${key.handle}")
        }
        val privateKeyReference = AndroidKeyStore.keyStore.getEntry(key.handle, null)
                as? KeyStore.PrivateKeyEntry ?: throw Error("Key ${key.handle} is not a private key")

        return Signature.getInstance(convertSignAlgorithmToAndroid(algorithm, key)).run {
            initSign(privateKeyReference.privateKey)
            update(data)
            sign()
        }
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {

        val s = Signature.getInstance(convertSignAlgorithmToAndroid(algorithm, key)).apply {
            initVerify()
            update(data)
        }
        return s.verify(signature)
    }

    override fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(algorithm.name)
        return digest.digest(data)
    }

    override fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey {

    }

    override fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKeyPair {

    }

    override fun deriveKey(
        algorithm: Algorithm,
        baseKey: CryptoKey,
        derivedKeyType: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importKey(
        format: KeyFormat,
        keyData: ByteArray,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        when (keyData.kty) {
            com.microsoft.did.sdk.crypto.keys.KeyType.RSA -> {
                val keyFactory = KeyFactory.getInstance(AndroidConstants.Rsa.value)
                if (keyData.d != null) { // Private RSA key being imported
                    keyFactory.generatePrivate(RSAPrivateKeySpec(
                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE)),
                        BigInteger(1, Base64.decode(keyData.d, Base64.URL_SAFE))
                    ))
                } else { // Public RSA key being imported
                    keyFactory.generatePublic(RSAPublicKeySpec(
                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE)),
                        BigInteger(1, Base64.decode(keyData.e, Base64.URL_SAFE))
                    ))
                }
            }
            com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve -> {
                val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
                if (keyData.d != null) { // Private EC key
                    keyFactory.generatePrivate(ECPrivateKeySpec(
                        BigInteger(1, Base64.decode(keyData.d, Base64.URL_SAFE)),
                        ECParameterSpec(
                            // EllipticCurve parameters,
                            ECPoint(
                                BigInteger(1, Base64.decode(keyData.x, Base64.URL_SAFE)),
                                BigInteger(1, Base64.decode(keyData.y, Base64.URL_SAFE))
                            )
                        )
                    ))
                } else { // Public EC key

                }
            }
            else -> throw Error("Cannot import JWK key type ${keyData.kty}")
        }
    }

    override fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wrapKey(
        format: KeyFormat,
        key: CryptoKey,
        wrappingKey: CryptoKey,
        wrapAlgorithm: Algorithm
    ): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unwrapKey(
        format: KeyFormat,
        wrappedKey: ByteArray,
        unwrappingKey: CryptoKey,
        unwrapAlgorithm: Algorithm,
        unwrappedKeyAlgorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun convertSignAlgorithmToAndroid(algorithm: Algorithm, cryptoKey: CryptoKey): String {
        return when (algorithm.name) {
            W3cCryptoApiConstants.EcDsa.value -> {
                val ecDsaParams = algorithm as EcdsaParams
                when (ecDsaParams.hash.name) {
                    W3cCryptoApiConstants.Sha1.value -> AndroidConstants.EcDsaSha1.value
                    W3cCryptoApiConstants.Sha224.value -> AndroidConstants.EcDsaSha224.value
                    W3cCryptoApiConstants.Sha256.value -> AndroidConstants.EcDsaSha256.value
                    W3cCryptoApiConstants.Sha384.value -> AndroidConstants.EcDsaSha384.value
                    W3cCryptoApiConstants.Sha512.value -> AndroidConstants.EcDsaSha512.value
                    else -> throw Error("Unsupported ECDSA hash algorithm: ${ecDsaParams.hash.name}")
                }
            }
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value -> {
                // The hash is indicated by the key's "algorithm" slot.
                val keyAlgorithm = cryptoKey.algorithm as? RsaHashedKeyAlgorithm ?: throw Error("Unsupported RSA key algorithm: ${cryptoKey.algorithm.name}")
                when (keyAlgorithm.hash.name) {
                    W3cCryptoApiConstants.Sha1.value -> AndroidConstants.RsSha1.value
                    W3cCryptoApiConstants.Sha224.value -> AndroidConstants.RsSha224.value
                    W3cCryptoApiConstants.Sha256.value -> AndroidConstants.RsSha256.value
                    W3cCryptoApiConstants.Sha384.value -> AndroidConstants.RsSha384.value
                    W3cCryptoApiConstants.Sha512.value -> AndroidConstants.RsSha512.value
                    else -> throw Error("Unsupported RSA hash algorithm: ${keyAlgorithm.hash.name}")
                }
            }
            else -> throw Error("Unsupported algorithm: ${algorithm.name}")
        }

    }
}