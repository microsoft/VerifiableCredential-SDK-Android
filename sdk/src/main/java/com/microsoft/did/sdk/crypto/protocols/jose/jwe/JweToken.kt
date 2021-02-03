package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.stringToByteArray
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.Exception
import java.util.*

class JweToken private constructor (
    private val serializer: Json,
    private var protected: ByteArray = ByteArray(0),
    private var plaintext: ByteArray = ByteArray(0),
    private var ciphertext: ByteArray = ByteArray(0),
    private var iv: ByteArray = ByteArray(0),
    private var aad: ByteArray = ByteArray(0),
    private var tag: ByteArray = ByteArray(0),
) {
    companion object {
        private fun serializeProtected(serializer: Json, protected: Map<String, String>): ByteArray {
            return stringToByteArray(serializer.encodeToString(MapSerializer(String.serializer(), String.serializer()), protected))
        }

        private fun parseProtected(serializer: Json, protected: ByteArray): MutableMap<String, String> {
            return serializer.decodeFromString(MapSerializer(String.serializer(), String.serializer()), byteArrayToString(protected)).toMutableMap()
        }

        private fun SafeJoinHeaders(unprotected: Map<String, String>?, header: Map<String, String>?): Map<String, String> {
            val outputMap = mutableMapOf<String, String>()
            if (unprotected != null) {
                outputMap.putAll(unprotected)
            }
            if (header != null) {
                if (header.keys.any { outputMap.containsKey(it) }) {
                    throw KeyException("Duplicate header keys detected")
                }
                outputMap.putAll(header)
            }
            return outputMap.toMap()
        }

        fun deserialize(jwe: String, serializer: Json = Json.Default): JweToken {
            //  protected.encrypted-key.iv.ciphertext.tag
            val compactRegex = Regex("([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)")
            val compactMatches = compactRegex.matchEntire(jwe.trim())
            var token: JweToken
            when {
                compactMatches != null -> { // compact
                    val protected = compactMatches.groupValues[1]
                    val key = compactMatches.groupValues[2]
                    val iv = compactMatches.groupValues[3]
                    val ciphertext = compactMatches.groupValues[4]
                    val tag = compactMatches.groupValues[5]
                    token = JweToken(
                        serializer,
                        protected = Base64Url.decode(protected),
                        ciphertext = Base64Url.decode(ciphertext),
                        iv = Base64Url.decode(iv),
                        aad = stringToByteArray(protected),
                        tag = Base64Url.decode(tag),
                    )
                    token.recipients.add(JweRecipient(
                        encryptedKey = key,
                        headers = emptyMap(),
                    ))
                }
                jwe.toLowerCase(Locale.ENGLISH).contains("\"recipients\"") -> { // general
                    val rawData = serializer.decodeFromString(JweGeneralJson.serializer(), jwe)
                    token = JweToken(
                        serializer,
                        protected = if (rawData.protected != null) { Base64Url.decode(rawData.protected) } else { ByteArray(0) },
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    for (recipient in rawData.recipients) {
                        token.recipients.add(JweRecipient(
                            encryptedKey = recipient.encryptedKey,
                            headers =  com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken.Companion.SafeJoinHeaders(rawData.unprotected, recipient.headers),
                        ))
                    }
                }
                else -> { // flat
                    val rawData = serializer.decodeFromString(JweFlatJson.serializer(), jwe)
                    token = JweToken(
                        serializer,
                        protected = if (rawData.protected != null) { Base64Url.decode(rawData.protected) } else { ByteArray(0) },
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    token.recipients.add(JweRecipient(
                        encryptedKey = rawData.encryptedKey,
                        headers = JweToken.Companion.SafeJoinHeaders(rawData.unprotected, rawData.header),
                    ))
                }
            }
            return token
        }
    }

    val contentAsByteArray: ByteArray
        get() = this.plaintext
    val contentAsString: String
        get() = byteArrayToString(this.plaintext)

    constructor(serializer: Json, plaintext: String): this(
        serializer,
        JweToken.Companion.serializeProtected(
            serializer, mapOf(JoseConstants.Enc.value to JoseConstants.AesGcm128.value)),
        plaintext.toByteArray())
    private var recipients: MutableList<JweRecipient> = mutableListOf()
    private var enc: String? = null
        get() = JweToken.Companion.parseProtected(serializer, protected)[JoseConstants.Enc.value]
    private var alg: String? = null
        get() = JweToken.Companion.parseProtected(serializer, protected)[JoseConstants.Alg.value]
    private var cek: CryptoKey? = null

    fun encrypt(cryptoOperations: CryptoOperations, protectedHeader: Map<String, String>? = null, secretKey: SecretKey? = null) {
        val setProtectedTo = if (protectedHeader != null) {
            JweToken.serializeProtected(serializer, protectedHeader)
        } else {
            this.protected
        }
        this.encrypt(cryptoOperations, setProtectedTo, secretKey)
    }

    private fun encrypt(cryptoOperations: CryptoOperations, protectedHeader: ByteArray, secretKey: SecretKey? = null) {
        var reEncrypt = false
        if (!protectedHeader.contentEquals(this.protected)) {
            this.protected = protectedHeader
            reEncrypt = true
        }
        if (enc.isNullOrEmpty()) {
            // default to aes 256 cbc for ios interop
            val headers = JweToken.Companion.parseProtected(serializer, protectedHeader)
            headers[JoseConstants.Enc.value] = JoseConstants.AesGcm128.value
            this.protected = JweToken.Companion.serializeProtected(serializer, headers)
            reEncrypt = true
        }
        if (reEncrypt || cek == null) {
            this.aad = this.protected
            // check with alg first. Key Derivation may be used
            val algGen = JwaCryptoConverter.jwaEncToKeyGenWebCrypto(this.alg)
            var subtle = cryptoOperations.subtleCryptoFactory.getSharedKeyEncrypter(algGen.name, SubtleCryptoScope.ALL)
            cek = subtle.deriveBits(algGen, secretKey, enc )
            // fallback to key generation and wrapping
            var encryption = JwaCryptoConverter.jwaEncToKeyGenWebCrypto(this.enc)
            val subtle = cryptoOperations.subtleCryptoFactory.getSymmetricEncrypter(encryption.name, SubtleCryptoScope.ALL)
            cek = subtle.generateKey(encryption, true, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt))
            this.iv = subtle.exportKey(KeyFormat.Raw, subtle.generateKey(encryption, true, listOf(KeyUsage.Encrypt)))
            encryption = JwaCryptoConverter.jwaEncToWebCrypto(enc, this.iv, this.aad)
            val cipherAndTag = subtle.encrypt(encryption, cek!!, this.plaintext)
            ciphertext = cipherAndTag.sliceArray( IntRange(0, cipherAndTag.count()-16))
            tag = cipherAndTag.sliceArray( IntRange(cipherAndTag.count()-16, cipherAndTag.count()))
            for (recipient in recipients) {
                recipient.publicKey?.let {
                    recipient.encryptedKey = wrapCekFor(cryptoOperations, it, recipient.headers[JoseConstants.Alg])
                }
            }
        }
    }

    private fun wrapCekFor(cryptoOperations: CryptoOperations, publicKey: PublicKey, alg: String?): String {
        val algorithmUsed = alg?: this.protected.alg
        if (algorithmUsed.isNullOrBlank()) {
            throw KeyException("Cannot wrap Content Encryption Key with public key, unknown algorithm.")
        }
        if (cek == null) {
            throw KeyException("There is no Content Encryption Key yet.")
        }
        val algorithm = JwaCryptoConverter.jwaAlgToWebCrypto(algorithmUsed)
        val subtle = cryptoOperations.subtleCryptoFactory.getKeyEncrypter(algorithm.name, SubtleCryptoScope.PUBLIC)
        val pubKey = subtle.importKey(KeyFormat.Jwk, publicKey.toJWK(), algorithm, true, listOf(KeyUsage.WrapKey))
        val encryptedKey = subtle.wrapKey(KeyFormat.Raw, cek!!, pubKey, algorithm)
        return Base64Url.encode(encryptedKey)
    }


    fun addRecipient(cryptoOperations: CryptoOperations, publicKey: PublicKey, headers: Map<String, String> = emptyMap()) {
        val encryptedKey = if (this.cek == null) {
            ""
        } else {
            wrapCekFor(cryptoOperations, publicKey, headers[JoseConstants.Alg])
        }
        this.recipients.add(JweRecipient(encryptedKey, headers, publicKey))
    }

    fun serialize(format: JweFormat = JweFormat.Compact): String {
        if (this.cek == null) {
            throw Exception("JweToken.encrypt() must be called before serialization")
        }
        if (this.recipients.count() == 0) {
            throw Exception("JWE encryption requires a recipient.")
        }
        val protectedEncoded = Base64Url.encode(protected)
        val initVec = Base64Url.encode(iv)
        val cipher = Base64Url.encode(ciphertext)
        val aadData = Base64Url.encode(aad)
        val tag = Base64Url.encode(tag)
        return when (format) {
            JweFormat.Compact -> {
                if (recipients.count() != 1) {
                    throw Exception("Compact JWE format requires a single recipient.")
                }
                val recip = recipients.first()
                // TODO: re-encrypt for aad/protected header reasons?
                "$protectedEncoded.${recip.encryptedKey}.$initVec.$cipher.$tag"
            }
            JweFormat.FlatJson -> {
                if (recipients.count() != 1) {
                    throw Exception("Flat JSON JWE format requires a single recipient.")
                }
                val recip = recipients.first()
                serializer.encodeToString(JweFlatJson.serializer(), JweFlatJson(
                    protected = protectedEncoded,
                    unprotected = null,
                    header = recip.headers,
                    encryptedKey = recip.encryptedKey,
                    iv = initVec,
                    aad = aadData,
                    ciphertext = cipher,
                    tag = tag
                ))
            }
            JweFormat.GeneralJson -> {
                serializer.encodeToString(JweGeneralJson.serializer(), JweGeneralJson(
                    protected = protectedEncoded,
                    unprotected = null,
                    iv = initVec,
                    aad = aadData,
                    ciphertext = cipher,
                    tag = tag,
                    recipients = recipients,
                ))
            }
        }
    }

    fun decrypt(cryptoOperations: CryptoOperations, privateKeys: List<PrivateKey>): ByteArray? {
        val protectedHeaders = JweToken.Companion.parseProtected(serializer, protected)
        for (recipient in recipients) {
            val joinedHeaders = JweToken.Companion.SafeJoinHeaders(protectedHeaders, recipient.headers)
            val alg = joinedHeaders[JoseConstants.Alg.value]
            val enc = joinedHeaders[JoseConstants.Enc.value]
            if (alg == null || enc == null) {
                // this is an invalid recipient, both alg and enc are required.
                continue;
            }
            val algAlgorithm = JwaCryptoConverter.jwaAlgToWebCrypto(alg)
            val encAlgorithm = JwaCryptoConverter.jwaEncToWebCrypto(enc, this.iv, this.aad)
            val encKey = Base64Url.decode(recipient.encryptedKey)
            val keyIdHint = joinedHeaders[JoseConstants.Kid.value]
            if (keyIdHint != null) {
                val hintKeys = privateKeys.filter { it.kid == keyIdHint }
                for (key in hintKeys) {
                    val plain = tryDecryptWithKey(cryptoOperations, key, encKey, encAlgorithm, algAlgorithm)
                    if (plain != null) {
                        return plain
                    }
                }
            } else {
                for (key in privateKeys) {
                    val plain = tryDecryptWithKey(cryptoOperations, key, encKey, encAlgorithm, algAlgorithm)
                    if (plain != null) {
                        return plain
                    }
                }
            }
        }
        return null
    }

    private fun tryDecryptWithKey(cryptoOperations: CryptoOperations, privateKey: PrivateKey, encryptedKey: ByteArray, enc: Algorithm, alg: Algorithm): ByteArray? {
        val algSubtle = cryptoOperations.subtleCryptoFactory.getKeyEncrypter(alg.name, SubtleCryptoScope.ALL)
        val key = algSubtle.importKey(KeyFormat.Jwk, privateKey.toJWK(), alg, false, listOf(KeyUsage.DeriveKey))
        try {
            val keyMaterial = algSubtle.decrypt(alg, key, encryptedKey)
            if (keyMaterial.size > 0) {
                val encSubtle = cryptoOperations.subtleCryptoFactory.getSymmetricEncrypter(enc.name, SubtleCryptoScope.PRIVATE)
                this.cek = encSubtle.importKey(KeyFormat.Raw, keyMaterial, enc, true, listOf(KeyUsage.Decrypt))
                val plaintext = encSubtle.decrypt(enc, this.cek!!, this.ciphertext)
                if (plaintext.size != 0) {
                    this.plaintext = plaintext
                }
                return plaintext
            }
        } finally {
            return null
        }
    }
}
