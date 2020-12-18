package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
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
                        headers = JweToken.Companion.SafeJoinHeaders(serializer, protected, null, null)
                    ))
                }
                jwe.toLowerCase(Locale.ENGLISH).contains("\"recipients\"") -> { // general
                    val rawData = serializer.decodeFromString(JweGeneralJson.serializer(), jwe)
                    val token = Jwetoken(
                        serializer,
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    for (val recipient in rawData.recipients) {
                        token.recipients.add(JweRecipient(
                            encryptedKey = recipient.encryptedKey,
                            headers = JweToken.Companion.safeJoinHeaders(serializers, rawData.protected, rawData.unprotected, recipient.header)
                        ))
                    }
                }
                else -> { // flat
                    val rawData = serializer.decodeFromString(JweFlatJson.serializer(), jwe)
                    val token = Jwetoken(
                        serializer,
                        ciphertext = Base64Url.decode(rawData.ciphertext),
                        iv = Base64Url.decode(rawData.iv),
                        aad = stringToByteArray(rawData.protected + "." + rawData.aad),
                        tag = Base64Url.decode(rawData.tag),
                    )
                    token.recipients.add(JweRecipient(
                        encryptedKey = rawData.encryptedKey,
                        headers = JweToken.Companion.safeJoinHeaders(serializers, rawData.protected, rawData.unprotected, rawData.header)
                    ))
                }
            }
            return token
        }
    }

    constructor(serializer: Json, plaintext: String): this(serializer, plaintext.toByteArray())
    private var recipients: MutableList<JweRecipient> = mutableListOf()
    private var alg: String = ""
    private var cek: ByteArray = ByteArray(0)


    fun encrypt(cryptoOperations: CryptoOperations, alg: String) {
        this.alg = alg
        // form cek if not present (if updated, update all recipient encrypted keys)
        // update iv, aad, tag
        // encrypt plaintext using cek and alg
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

    fun addRecipient(cryptoOperations: CryptoOperations, publicKey: PublicKey, headers: Map<String, String> = emptyMap()) {
        var encryptedKey: ByteArray
        if (this.cek.size == 0) {
            encryptedKey = ByteArray(0)
        } else {
            // encrypt cek using 'enc', or public keys default method
        }
        this.recipients.add( JweRecipient(encryptedKey, headers, publicKey) )
    }

    fun decrypt(cryptoOperations: CryptoOperations, privateKeys: List<PrivateKey>): ByteArray? {
        //

    }
}
