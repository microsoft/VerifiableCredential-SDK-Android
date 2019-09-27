import android.annotation.TargetApi
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.AndroidKeyHandle
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithms.AesKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithms.RsaHashedKeyGenParams
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import java.math.BigInteger
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.*
import javax.crypto.KeyGenerator

class AndroidSubtle: SubtleCrypto {
    override fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        // verify we're signing with a private key
        if (key.type != KeyType.Private) {
            throw Error("Sign must use a private key")
        }
        // key's handle should be an Android keyStore key reference.
        val handle = cryptoKeyToPrivateKey(key)
        return Signature.getInstance(signAlgorithmToAndroid(algorithm, key)).run {
            initSign(handle.privateKey)
            update(data)
            sign()
        }
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val handle = cryptoKeyToPublicKey(key)
        val s = Signature.getInstance(signAlgorithmToAndroid(algorithm, key)).apply {
            initVerify(handle)
            update(data)
        }
        return s.verify(signature)
    }

    override fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(algorithm.name)
        return digest.digest(data)
    }

    override fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey {
        val secret = when(algorithm.name) {
            W3cCryptoApiConstants.AesCbc.value, W3cCryptoApiConstants.AesCtr.value,
            W3cCryptoApiConstants.AesGcm.value, W3cCryptoApiConstants.AesKw.value -> {
                val generator = KeyGenerator.getInstance(AndroidConstants.Aes.value)
                val alg = algorithm as AesKeyGenParams
                generator.init(alg.length.toInt())
                generator.generateKey()
            }
            else -> throw Error("Unsupported symmetric key algorithm: ${algorithm.name}")
        }
        return CryptoKey(
            type = KeyType.Secret,
            extractable = extractable,
            usages = keyUsages,
            handle = secret,
            algorithm = algorithm
        )
    }

    @TargetApi(23)
    override fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKeyPair {
        if (!algorithm.additionalParams.containsKey(AndroidConstants.KeyReference.value)){
            throw Error("Algorithm must contain an additional parameter \"${AndroidConstants.KeyReference.value}\"")
        }
        val alias = AndroidKeyStore.checkOrCreateKeyId(algorithm.additionalParams[AndroidConstants.KeyReference.value] as String, null)
        val keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithmToAndroid(algorithm), AndroidKeyStore.provider)
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                keyPairUsageToAndroid(keyUsages)
            ).setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .build()
        )
        val keyPair = keyPairGenerator.genKeyPair()
        // get the private key reference
        val entry = AndroidKeyStore.keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        // convert keypair.
        return CryptoKeyPair(
            CryptoKey(
                KeyType.Public,
                extractable,
                algorithm,
                keyUsages,
                AndroidKeyHandle(
                    alias,
                    keyPair.public
                )
            ),
            CryptoKey(
                KeyType.Private,
                false,
                algorithm,
                keyUsages,
                AndroidKeyHandle(
                    alias,
                    entry
                )
            )
        )
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
            com.microsoft.did.sdk.crypto.keys.KeyType.RSA.value -> {
                val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
                if (keyData.d != null) { // Private RSA key being imported
                    if (!AndroidKeyStore.keyStore.isKeyEntry(keyData.kid ?: "")) {
                        throw Error("Software private keys are not supported.")
                    }
                    val entry = AndroidKeyStore.keyStore.getEntry(keyData.kid!!, null) as?
                            KeyStore.PrivateKeyEntry ?: throw Error("Key must be a private key")
                    return CryptoKey(
                        KeyType.Private,
                        extractable,
                        jwkAlgorithmToCryptoKeyAlgorithm(keyData.alg, entry.certificate.publicKey),
                        keyUsages,
                        entry
                    )
                } else { // Public RSA key being imported
                    val key = keyFactory.generatePublic(RSAPublicKeySpec(
                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE)),
                        BigInteger(1, Base64.decode(keyData.e, Base64.URL_SAFE))
                    ))
                    return CryptoKey(
                        KeyType.Public,
                        extractable,
                        jwkAlgorithmToCryptoKeyAlgorithm(keyData.alg , key),
                        keyUsages,
                        key
                    )
                }
            }
            com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value -> {
                TODO("Standard Elliptic Curves are not currently supported.")
            }
            else -> throw Error("Cannot import JWK key type ${keyData.kty}")
        }
    }

    override fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        val internalHandle = key.handle as? AndroidKeyHandle ?: throw Error("Unknown format for CryptoKey passed")
        return when (internalHandle.key) {
            is PublicKey -> {
                AndroidKeyStore.androidPublicKeyToPublicKey(internalHandle.alias, internalHandle.key).toJWK()
            }
            is KeyStore.PrivateKeyEntry -> {
                AndroidKeyStore.androidPrivateKeyToPrivateKey(internalHandle.alias, internalHandle.key).toJWK()
            }
            else -> {
                throw Error("Unknown CryptoKey format")
            }
        }
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

    private fun cryptoKeyToPublicKey(key: CryptoKey): PublicKey {
        val internalHandle = key.handle as? AndroidKeyHandle ?: throw Error("Unknown format for CryptoKey passed")
        return internalHandle.key as? PublicKey ?: throw Error("Private key passed when a public key was expected")
    }

    private fun cryptoKeyToPrivateKey(key: CryptoKey): KeyStore.PrivateKeyEntry {
        val internalHandle = key.handle as? AndroidKeyHandle ?: throw Error("Unknown format for CryptoKey passed")
        return internalHandle.key as? KeyStore.PrivateKeyEntry ?: throw Error("Software private keys are not supported.")
    }


    private fun signAlgorithmToAndroid(algorithm: Algorithm, cryptoKey: CryptoKey): String {
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

    private fun keyPairAlgorithmToAndroid(algorithm: Algorithm): String {
        return when (algorithm.name) {
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value -> AndroidConstants.Rsa.value
            W3cCryptoApiConstants.EcDsa.value -> AndroidConstants.Ec.value
            else -> throw Error("Unknown algorithm used: ${algorithm.name}")
        }
    }

    private fun keyPairUsageToAndroid(usages: List<KeyUsage>): Int {
        var flags = 0
        usages.forEach { usage ->
            flags = flags.or(when (usage) {
                KeyUsage.Decrypt -> KeyProperties.PURPOSE_DECRYPT
                KeyUsage.Encrypt -> KeyProperties.PURPOSE_ENCRYPT
                KeyUsage.Sign -> KeyProperties.PURPOSE_SIGN
                KeyUsage.Verify -> KeyProperties.PURPOSE_VERIFY
                KeyUsage.WrapKey -> KeyProperties.PURPOSE_WRAP_KEY
                KeyUsage.UnwrapKey -> KeyProperties.PURPOSE_WRAP_KEY
                else -> 0
            })
        }
        return flags
    }

    private fun jwkAlgorithmToCryptoKeyAlgorithm(alg: String?, key: PublicKey): Algorithm {
        return when (alg) {
            null -> Algorithm("unknown")
            JoseConstants.Rs256.value, JoseConstants.Rs384.value, JoseConstants.Rs512.value -> {
                val length = Regex("RS(\\d+)").matchEntire(alg)!!.groupValues.first()
                val rsaKey = key as RSAPublicKey
                return RsaHashedKeyAlgorithm(
                    modulusLength = rsaKey.modulus.toLong().toULong(),
                    publicExponent = rsaKey.publicExponent.toLong().toULong(),
                    hash = Sha.get(length.toInt())
                )
            }
            else -> throw Error("Unknown JWK algorithm: $alg")
        }
    }
}