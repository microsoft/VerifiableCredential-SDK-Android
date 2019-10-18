package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStoreListItem
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.utilities.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import kotlin.collections.Map

/**
 * Class for containing JWS token operations.
 * @class
 */
class JwsToken private constructor(private val payload: String, signatures: List<JwsSignature> = emptyList()) {

    val signatures: MutableList<JwsSignature> = signatures.toMutableList()

    companion object {
        fun deserialize(jws: String): JwsToken {
            val compactRegex = Regex("([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)")
            val compactMatches = compactRegex.matchEntire(jws.trim())
            if (compactMatches != null) {
                // compact JWS format
                val protected = compactMatches.groupValues[1]
                val payload = compactMatches.groupValues[2]
                val signature = compactMatches.groupValues[3]
                val jwsSignatureObject = JwsSignature(
                    protected = protected,
                    header =  null,
                    signature = signature
                )
                return JwsToken(payload, listOf(jwsSignatureObject))
            } else if (jws.toLowerCase().contains("\"signatures\"")) { // check for signature or signatures
                // GENERAL
                val token = MinimalJson.serializer.parse(JwsGeneralJson.serializer(), jws)
                return JwsToken(
                    payload = token.payload,
                    signatures = token.signatures
                )
            } else if (jws.toLowerCase().contains("\"signature\"")) {
                // Flat
                val token = MinimalJson.serializer.parse(JwsFlatJson.serializer(), jws)
                return JwsToken(
                    payload = token.payload,
                    signatures = listOf(JwsSignature(
                        protected = token.protected,
                        header = token.header,
                        signature = token.signature
                    ))
                )
            } else {
                // Unidentifiable garbage
                throw Error("Unable to parse JWS token.")
            }
        }
    }

    constructor(content: ByteArray): this(Base64Url.encode(content), emptyList()) {}

    constructor(content: String): this(Base64Url.encode(stringToByteArray(content)), emptyList()) {}

    /**
     * Serialize a JWS token object from token.
     */
    fun serialize (format: JwsFormat = JwsFormat.Compact): String {
        return when(format) {
            JwsFormat.Compact -> {
                val jws = intermediateCompactSerialize()
                "${jws.protected}.${jws.payload}.${jws.signature}"
            }
            JwsFormat.FlatJson -> {
                val jws = intermediateFlatJsonSerialize()
                MinimalJson.serializer.stringify(JwsFlatJson.serializer(), jws)
            }
            JwsFormat.GeneralJson -> {
                val jws = intermediateGeneralJsonSerialize()
            MinimalJson.serializer.stringify(JwsGeneralJson.serializer(), jws)
            }
            else -> {
                throw Error("Unknown JWS format: $format")
            }
        }
    }

    fun intermediateCompactSerialize(): JwsCompact {
        val signature = this.signatures.firstOrNull() ?: throw Error("This JWS token contains no signatures")
        return JwsCompact(
            protected = signature.protected,
            payload = this.payload,
            signature = signature.signature
        )
    }

    fun intermediateFlatJsonSerialize(): JwsFlatJson {
        val signature = this.signatures.firstOrNull() ?: throw Error("This JWS token contains no signatures")
        return JwsFlatJson(
            protected = signature.protected,
            header = signature.header,
            payload = this.payload,
            signature = signature.signature
        )
    }

    fun intermediateGeneralJsonSerialize(): JwsGeneralJson {
        if (this.signatures.count() == 0) {
            throw Error("This JWS token contains no signatures")
        }
        return JwsGeneralJson(
            payload = this.payload,
            signatures = this.signatures.toList()
        )
    }

    /**
     * Adds a signature using the given key
     * @param signingKeyReference reference to signing key
     * @param cryptoOperations CryptoOperations used to form the signatures
     * @param header optional headers added to the signature
     */
    @ImplicitReflectionSerializer
    fun sign(signingKeyReference: String, cryptoOperations: CryptoOperations, header: Map<String, String> = emptyMap()) {
        // 1. Get the signing key's metadata
        val signingKey = cryptoOperations.keyStore.getPrivateKey(signingKeyReference).getKey()

        // 3. Compute headers
        val headers = header.toMutableMap()
        val protected = mutableMapOf<String, String>()

        var algorithmName = ""

        if (!headers.containsKey(JoseConstants.Alg.value)) {
            if (signingKey.alg != null) {
                algorithmName = signingKey.alg!!
                protected[JoseConstants.Alg.value] = algorithmName
            } else {
                throw Error("No algorithm defined for key $signingKeyReference")
            }
        } else {
            algorithmName = headers[JoseConstants.Alg.value]!!
        }

        if (!headers.containsKey(JoseConstants.Kid.value)) {
            protected[JoseConstants.Kid.value] = signingKey.kid
            println("Using key ${protected[JoseConstants.Kid.value]}")
        }

        var encodedProtected = ""
        if (protected.isNotEmpty()) {
            val jsonProtected = MinimalJson.serializer.stringify(protected)
            encodedProtected = Base64Url.encode(stringToByteArray(jsonProtected))
        }

        val signatureInput = stringToByteArray("$encodedProtected.${this.payload}")

        val signature = cryptoOperations.sign(
            signatureInput, signingKeyReference,
            JwaCryptoConverter.jwaAlgToWebCrypto(algorithmName))

        val signatureBase64 = Base64Url.encode(signature)

        this.signatures.add(
            JwsSignature(
                protected = encodedProtected,
                header = headers,
                signature = signatureBase64
        ))
    }

    /**
     *Verify the JWS signatures
     */
    @ImplicitReflectionSerializer
    fun verify(cryptoOperations: CryptoOperations, publicKeys: List<PublicKey> = emptyList(), all: Boolean = false) {
        val results = this.signatures.map {
            val fullyQuantifiedKid = it.getKid() ?: ""
            val kid = JwaCryptoConverter.extractDidAndKeyId(fullyQuantifiedKid).second
            val signatureInput = "${it.protected}.${this.payload}"
            val publicKey = cryptoOperations.keyStore.getPublicKeyById(kid)
            if (publicKey != null) {
                println("Internal key ${publicKey.kid} attempted")
                verifyWithKey(cryptoOperations, signatureInput, it, publicKey)
            } else {
                // use one of the provided public Keys
                val key = publicKeys.firstOrNull {
                    kid.endsWith(it.kid!!)
                }
                when {
                    key != null -> {
                        println("key ${key.kid} attempted")
                        verifyWithKey(cryptoOperations, signatureInput, it, key)
                    }
                    publicKeys.isNotEmpty() -> {
                        println("first publickey attempted")
                        verifyWithKey(cryptoOperations, signatureInput, it, publicKeys.first())
                    }
                    else -> {
                        println("No keys attempted")
                        false
                    }
                }
            }
        }
        if (!if (all) {
            results.reduce{
                    result, valid -> result && valid
            }
        } else {
            results.reduce {
                result, valid -> result || valid
            }
        }) {
            throw Error("Invalid Signature")
        }
    }

    @ImplicitReflectionSerializer
    private fun verifyWithKey(crypto: CryptoOperations, data: String, signature: JwsSignature, key: PublicKey): Boolean {
        val alg = signature.getAlg() ?: throw Error("This signature contains no algorithm.")
        val subtleAlg = JwaCryptoConverter.jwaAlgToWebCrypto(alg)
        val subtle = crypto.subtleCryptoFactory.getMessageSigner(subtleAlg.name, SubtleCryptoScope.Public)
        val cryptoKey = subtle.importKey(KeyFormat.Jwk, key.toJWK(), subtleAlg,
            true, key.key_ops ?: listOf(KeyUsage.Verify))
        val rawSignature = Base64Url.decode(signature.signature)
        val rawData = stringToByteArray(data)
        return subtle.verify(subtleAlg, cryptoKey, rawSignature, rawData)
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return byteArrayToString(Base64Url.decode(this.payload))
    }

}