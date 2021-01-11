package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Pbkdf2Params
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import kotlinx.serialization.json.Json
import org.spongycastle.asn1.ASN1ObjectIdentifier
import org.spongycastle.asn1.x509.AlgorithmIdentifier
import org.spongycastle.jcajce.spec.PBKDF2KeySpec
import javax.crypto.SecretKeyFactory

/**
 * Subtle Crypto provider for PBKDF2 according to @see https://www.w3.org/TR/WebCryptoAPI/#pbkdf2
 */
class Pbkdf2Provider: Provider() {
    companion object {
        val subtle = Subtle(setOf(Pbkdf2Provider()), Json.Default)
    }

    override val name: String
        get() = W3cCryptoApiConstants.Pbkdf2.value
    override val privateKeyUsage: Set<KeyUsage>?
        get() = emptySet()
    override val publicKeyUsage: Set<KeyUsage>?
        get() = emptySet()
    override val symmetricKeyUsage: Set<KeyUsage>?
        get() = setOf(KeyUsage.DeriveBits)

    override fun onDeriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        val params = algorithm as Pbkdf2Params
        val secretFactory = when(algorithm.hash.name) {
            W3cCryptoApiConstants.HmacSha256.value -> SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA256")
            W3cCryptoApiConstants.HmacSha384.value -> SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA384")
            W3cCryptoApiConstants.HmacSha512.value -> SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA512")
            else -> throw AlgorithmException("Unsupported hash algorithm ${algorithm.hash.name}")
        }
        val secretKey = secretFactory.generateSecret(PBKDF2KeySpec(
            byteArrayToString(baseKey.handle as ByteArray).toCharArray(),
            params.salt,
            params.iterations.toInt(),
            length.toInt(),
            AlgorithmIdentifier(ASN1ObjectIdentifier(W3cCryptoApiConstants.Pbkdf2.value))
        ))

        return secretKey.encoded
    }

    override fun onImportKey(
        format: KeyFormat,
        keyData: ByteArray,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        if (format != KeyFormat.Raw) {
            throw AlgorithmException("PBKDF2 importKey expects a raw secret")
        }
        val allowedKeyUsage = setOf(KeyUsage.DeriveKey, KeyUsage.DeriveBits)
        if (keyUsages.any { !allowedKeyUsage.contains(it) }) {
            throw AlgorithmException("PBKDF2 importKey only allows DeriveBits or DeriveKey usage")
        }
        if (!extractable) {
            throw AlgorithmException("PBKDF2 importKey should not be extractable")
        }
        return CryptoKey(
            type = KeyType.Secret,
            extractable = false,
            algorithm = Algorithm(
                name = this.name
            ),
            usages = keyUsages.toList(),
            handle = keyData
        )
    }
}