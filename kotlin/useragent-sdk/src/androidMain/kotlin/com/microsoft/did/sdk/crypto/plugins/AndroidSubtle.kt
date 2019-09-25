import android.util.Base64
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.AndroidInternalKeyHandle
import com.microsoft.did.sdk.crypto.keys.AndroidPublicKeyHandle
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import java.math.BigInteger
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

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
        val internalHandle = key.handle as? AndroidInternalKeyHandle
        if (internalHandle != null) {
            return Signature.getInstance(convertSignAlgorithmToAndroid(algorithm, key)).run {
                initSign(internalHandle.key.privateKey)
                update(data)
                sign()
            }
        } else {
            TODO("Support software private keys")
        }
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val handle = key.handle as? AndroidPublicKeyHandle ?: throw Error("Unknown format for CryptoKey passed")
        val s = Signature.getInstance(convertSignAlgorithmToAndroid(algorithm, key)).apply {
            initVerify(handle.key)
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
            com.microsoft.did.sdk.crypto.keys.KeyType.RSA.value -> {
                val keyFactory = KeyFactory.getInstance(AndroidConstants.Rsa.value)
                if (keyData.d != null) { // Private RSA key being imported
                    throw Error("Importing private RSA keys is not supported")
//                    keyFactory.generatePrivate(RSAPrivateKeySpec(
//                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE)),
//                        BigInteger(1, Base64.decode(keyData.d, Base64.URL_SAFE))
//                    ))
                } else { // Public RSA key being imported
                    val key = keyFactory.generatePublic(RSAPublicKeySpec(
                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE)),
                        BigInteger(1, Base64.decode(keyData.e, Base64.URL_SAFE))
                    ))
                    return CryptoKey(
                        KeyType.Public,
                        extractable,
                        convertJwkAlgorithmToCryptoKeyAlgorithm(keyData.alg , key),
                        keyUsages,
                        AndroidPublicKeyHandle(key)
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

    private fun convertJwkAlgorithmToCryptoKeyAlgorithm(alg: String?, key: Key): Algorithm {
        when (alg) {
            null -> Algorithm("unknown")
            JoseConstants.Rs256.value, JoseConstants.Rs384.value, JoseConstants.Rs512.value -> {
                val length = Regex("RS(\\d+)").matchEntire(alg).groupValues.first()
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