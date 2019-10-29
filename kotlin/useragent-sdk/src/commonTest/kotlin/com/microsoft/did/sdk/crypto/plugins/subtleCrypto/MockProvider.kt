package com.microsoft.did.sdk.crypto.plugins.subtleCrypto

import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.utilities.*
import kotlinx.serialization.Serializable
import kotlin.random.Random

class MockProvider() : Provider(ConsoleLogger()) {
    override val name: String = W3cCryptoApiConstants.RsaOaep.value
    override val privateKeyUsage: Set<KeyUsage>? = setOf(
        KeyUsage.Sign,
        KeyUsage.Decrypt
    )
    override val publicKeyUsage: Set<KeyUsage>? = setOf(
        KeyUsage.Verify,
        KeyUsage.Encrypt
    )
    override val symmetricKeyUsage: Set<KeyUsage>? = null

    @Serializable
    private data class Datagram(
        val keyId: String,
        val data: String
    ) {
        fun getData(): ByteArray {
            return stringToByteArray(data)
        }

        companion object {
            fun serialize(key: CryptoKey, data: ByteArray): ByteArray {
                val datagram = Datagram(key.handle as String, byteArrayToString(data))
                val json = MinimalJson.serializer.stringify(serializer(), datagram)
                return stringToByteArray(json)
            }

            fun deserialize(datagram: ByteArray): Datagram {
                return MinimalJson.serializer.parse(serializer(), byteArrayToString(datagram))
            }
        }
    }

    override fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        return Datagram.serialize(key, data)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val datagram = Datagram.deserialize(signature)
        if (key.handle != datagram.keyId) {
            throw Error("Incorrect key used")
        }
        datagram.getData().forEachIndexed {
                index, byte ->
            if (data[index] != byte) {
                throw Error("Signed data differs at byte $index")
            }
        }
        return true
    }

    override fun checkGenerateKeyParams(algorithm: Algorithm) {
        // do nothing!
    }

    override fun onGenerateKeyPair(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKeyPair {
        val kid = Base64Url.encode(Random.Default.nextBytes(8))
        return CryptoKeyPair(
            publicKey = CryptoKey(
                KeyType.Public,
                extractable,
                algorithm,
                handle = kid,
                usages = this.publicKeyUsage?.toList() ?: emptyList()
            ),
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                algorithm,
                handle = kid,
                usages = this.privateKeyUsage?.toList() ?: emptyList()
            )
        )
    }

    override fun onImportKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        return CryptoKey(
            KeyType.Secret,
            extractable = extractable,
            handle = keyData.kid ?: Base64Url.encode(Random.Default.nextBytes(8)),
            usages = keyUsages.toList(),
            algorithm = algorithm
        )
    }

    override fun onExportKeyJwk(key: CryptoKey): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = W3cCryptoApiConstants.RsaOaep.value,
            kid = key.handle as String
        )
    }
}