package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsGeneralJson
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.stringToByteArray
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.util.*

class JweToken private constructor (
    private val serializer: Json,
    private var plaintext: ByteArray = ByteArray(0),
    private var ciphertext: ByteArray = ByteArray(0),
    private var iv: ByteArray = ByteArray(0),
    private var aad: ByteArray = ByteArray(0),
    private var tag: ByteArray = ByteArray(0),
) {
    companion object {
        private fun praseProtected(serializer: Json, protected: String): Map<String, String> {
            val jsonProtected = Base64Url.decode(protected)
            return serializer.decodeFromString(MapSerializer(String.serializer(), String.serializer()), byteArrayToString(jsonProtected))
        }

        private fun SafeJoinHeaders(serializer: Json, protected: String?, unprotected: Map<String, String>?, header: Map<String, String>?): Map<String, String> {
            val outputMap = mutableMapOf<String, String>()
           if (protected != null) {
                val protectedMap = JweToken.Companion.praseProtected(serializer, protected)
               outputMap.putAll(protectedMap)
            }
            if (unprotected != null) {
                if (unprotected.keys.any { outputMap.containsKey(it) }) {
                    throw KeyException("Duplicate header keys detected")
                }
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


        fun deserialize(jwe: String, serializer: Json): JweToken {
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
                        ciphertext = Base64Url.decode(ciphertext),
                        iv = Base64Url.decode(iv),
                        aad = stringToByteArray(protected + "."),
                        tag = Base64Url.decode(tag),
                    )
                    token.recipients.add(JweRecipient(
                        encryptedKey = key,
                        headers = JweToken.Companion.SafeJoinHeaders(serializer, protected, null, null),
                    ))
                }
                jwe.toLowerCase(Locale.ENGLISH).contains("\"recipients\"") -> { // general
                    val rawData = serializer.decodeFromString(JweGeneralJson.serializer(), jwe)
                    val token = JweToken(
                        serializer,
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    for (val recipient in rawData.recipients) {
                        token.recipients.add(JweRecipient(
                            encryptedKey = recipient.encryptedKey,
                            headers = JweToken.Companion.safeJoinHeaders(serializers, rawData.protected, rawData.unprotected, recipient.header),
                        ))
                    }
                }
                else -> { // flat
                    val rawData = serializer.decodeFromString(JweFlatJson.serializer(), jwe)
                    val token = JweToken(
                        serializer,
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    token.recipients.add(JweRecipient(
                        encryptedKey = rawData.encryptedKey,
                        headers = JweToken.Companion.safeJoinHeaders(serializers, rawData.protected, rawData.unprotected, rawData.header),
                    ))
                }
            }
            return token
        }
    }

    constructor(serializer: Json, plaintext: String): this(serializer, plaintext.toByteArray())
    private var recipients: MutableList<JweRecipient> = mutableListOf()
    private var enc: String = ""
    private var cek: CryptoKey? = null

    fun encrypt(cryptoOperations: CryptoOperations) {
        // default to aes 128 gcm
        if (enc.isEmpty()) {
            enc = JoseConstants.AesGcm128.value
        }
        this.encrypt(cryptoOperations, enc)
    }

    fun encrypt(cryptoOperations: CryptoOperations, enc: String) {
        var reEncrypt = false
        if (this.enc != enc) {
            this.enc = enc
            reEncrypt = true
        }
        if (reEncrypt || cek == null) {
            val encryption = JwaCryptoConverter.jwkAlgToKeyGenWebCrypto(enc)
            var subtle = cryptoOperations.subtleCryptoFactory.getSymmetricEncrypter(encryption.name, SubtleCryptoScope.ALL)
            cek = subtle.generateKey(encryption, true, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt))
            iv = subtle.exportKey(KeyFormat.Raw, subtle.generateKey(encryption, true, listOf(KeyUsage.Encrypt)))
            algorithm.additionalParams["iv"] = iv
            algorithm.additionalParams["aad"] = this.aad
            this.ciphertext = subtle.encrypt(encryption, key, this.plaintext)
            for (val recipient in recipients) {
                recipient.encryptedKey = wrapCekFor(cryptoOperations, recipient.publicKey, recipient.headers[JoseConstants.Alg])
            }
        }
    }

    private fun wrapCekFor(cryptoOperations: CryptoOperations, publicKey: PublicKey, alg: String?): ByteArray {
        val algorithmUsed = alg?: publicKey.alg
        if (algorithmUsed.isNullOrBlank()) {
            throw KeyException("Cannot wrap Content Encryption Key with public key, unknown algorithm.")
        }
        if (cek == null) {
            throw KeyException("There is no Content Encryption Key yet.")
        }
        val algorithm = JwaCryptoConverter.jwaAlgToWebCrypto(algorithmUsed)
        val subtle = cryptoOperations.subtleCryptoFactory.getKeyEncrypter(algorithm.name, SubtleCryptoScope.PUBLIC)
        val encryptedKey = subtle.wrapKey(KeyFormat.Raw, cek, publicKey, algorithm)
        return encryptedKey
    }


    fun addRecipient(cryptoOperations: CryptoOperations, publicKey: PublicKey, headers: Map<String, String> = emptyMap()) {
        var encryptedKey: ByteArray
        if (this.cek.size == 0) {
            encryptedKey = ByteArray(0)
        } else {
            encryptedKey = wrapCekFor(cryptoOperations, publicKey, headers[JoseConstants.Alg])
        }
        this.recipients.add( JweRecipient(encryptedKey, headers, publicKey) )
    }

    fun serialize(format: JweFormat = JweFormat.Compact): String {
        if (this.cek.size == 0) {
            throw Exception("JweToken.encrypt() must be called before serialization")
        }
        if (this.recipients.count() == 0) {
            throw Exception("JWE encryption requires a recipient.")
        }
        if (format == JweFormat)
        return when (format) {
            JweFormat.Compact -> {
                if (recipients.count() != 1) {
                    throw Exception("Compact JWE format requires a single recipient.")
                }
            }
            JweFormat.FlatJson -> {
                if (recipients.count() != 1) {
                    throw Exception("Flat JSON JWE format requires a single recipient.")
                }
            }
            JweFormat.GeneralJson -> {

            }
        }

    }

    fun decrypt(cryptoOperations: CryptoOperations, privateKeys: List<PrivateKey>): ByteArray? {

    }
}
