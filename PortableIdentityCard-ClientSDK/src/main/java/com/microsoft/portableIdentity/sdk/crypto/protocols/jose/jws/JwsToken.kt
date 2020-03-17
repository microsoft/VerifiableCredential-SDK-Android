package com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.*
import kotlin.collections.Map

/**
 * Class for containing JWS token operations.
 * @class
 */
class JwsToken private constructor(private val payload: String,
                                   signatures: List<JwsSignature> = emptyList(),
                                   private val logger: ILogger): JoseToken {

    val signatures: MutableList<JwsSignature> = signatures.toMutableList()

    companion object {
        fun deserialize(jws: String, logger: ILogger): JwsToken {
            val compactRegex = Regex("([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)")
            val compactMatches = compactRegex.matchEntire(jws.trim())
            if (compactMatches != null) {
                // compact JWS format
                println("Compact format detected")
                val protected = compactMatches.groupValues[1]
                val payload = compactMatches.groupValues[2]
                val signature = compactMatches.groupValues[3]
                val jwsSignatureObject = JwsSignature(
                    protected = protected,
                    header =  null,
                    signature = signature
                )
                return JwsToken(payload, listOf(jwsSignatureObject), logger = logger)
            } else if (jws.toLowerCase().contains("\"signatures\"")) { // check for signature or signatures
                // GENERAL
                println("General format detected")
                val token = Serializer.parse(JwsGeneralJson.serializer(), jws)
                return JwsToken(
                    payload = token.payload,
                    signatures = token.signatures,
                    logger = logger
                )
            } else if (jws.toLowerCase().contains("\"signature\"")) {
                // Flat
                println("Flat format detected")
                val token = Serializer.parse(JwsFlatJson.serializer(), jws)
                return JwsToken(
                    payload = token.payload,
                    signatures = listOf(JwsSignature(
                        protected = token.protected,
                        header = token.header,
                        signature = token.signature
                    )),
                    logger = logger
                )
            } else {
                // Unidentifiable garbage
                throw logger.error("Unable to parse JWS token.")
            }
        }
    }

    constructor(content: ByteArray, logger: ILogger): this(Base64Url.encode(content, logger = logger), emptyList(), logger) {}

    constructor(content: String, logger: ILogger): this(Base64Url.encode(stringToByteArray(content), logger = logger), emptyList(), logger) {}

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
                Serializer.stringify(JwsFlatJson.serializer(), jws)
            }
            JwsFormat.GeneralJson -> {
                val jws = intermediateGeneralJsonSerialize()
                Serializer.stringify(JwsGeneralJson.serializer(), jws)
            }
            else -> {
                throw logger.error("Unknown JWS format: $format")
            }
        }
    }

    fun intermediateCompactSerialize(): JwsCompact {
        val signature = this.signatures.firstOrNull() ?: throw logger.error("This JWS token contains no signatures")
        return JwsCompact(
            protected = signature.protected,
            payload = this.payload,
            signature = signature.signature
        )
    }

    fun intermediateFlatJsonSerialize(): JwsFlatJson {
        val signature = this.signatures.firstOrNull() ?: throw logger.error("This JWS token contains no signatures")
        return JwsFlatJson(
            protected = signature.protected,
            header = signature.header,
            payload = this.payload,
            signature = signature.signature
        )
    }

    fun intermediateGeneralJsonSerialize(): JwsGeneralJson {
        if (this.signatures.count() == 0) {
            throw logger.error("This JWS token contains no signatures")
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
                throw logger.error("No algorithm defined for key $signingKeyReference")
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
            val jsonProtected = Serializer.stringify(protected, String::class, String::class)
            encodedProtected = Base64Url.encode(stringToByteArray(jsonProtected), logger = logger)
        }

        val signatureInput = stringToByteArray("$encodedProtected.${this.payload}")

        val signature = cryptoOperations.sign(
            signatureInput, signingKeyReference,
            JwaCryptoConverter.jwaAlgToWebCrypto(algorithmName, logger = logger))

        val signatureBase64 = Base64Url.encode(signature, logger = logger)

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
    fun verify(cryptoOperations: CryptoOperations, publicKeys: List<PublicKey> = emptyList(), all: Boolean = false): Boolean {
        val results = this.signatures.map {
            val fullyQuantifiedKid = it.getKid(logger = logger) ?: ""
            val kid = JwaCryptoConverter.extractDidAndKeyId(fullyQuantifiedKid).second
            println("Finding matching key for \"$kid\"")
            val signatureInput = "${it.protected}.${this.payload}"
            println("SDATA: $signatureInput")
            val publicKey = cryptoOperations.keyStore.getPublicKeyById(kid)
            if (publicKey != null) {
                println("Internal key ${publicKey.kid} attempted")
                verifyWithKey(cryptoOperations, signatureInput, it, publicKey)
            } else {
                // use one of the provided public Keys
                val key = publicKeys.firstOrNull {
                    it.kid.endsWith(kid)
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
        if ((return !if (all) {
                results.reduce{ result, valid -> result && valid
                }
            } else {
                results.reduce {
                    result, valid -> result || valid
                }
            })
        ) {
            // TODO: fix signature verification on the enterprise agent?
            // throw logger.error("Invalid Signature")
        }
    }

    private fun verifyWithKey(crypto: CryptoOperations, data: String, signature: JwsSignature, key: PublicKey): Boolean {
        val alg = signature.getAlg(logger = logger) ?: throw logger.error("This signature contains no algorithm.")
        val subtleAlg = JwaCryptoConverter.jwaAlgToWebCrypto(alg, logger = logger)
        val subtle = crypto.subtleCryptoFactory.getMessageSigner(subtleAlg.name, SubtleCryptoScope.Public)
        val cryptoKey = subtle.importKey(KeyFormat.Jwk, key.toJWK(), subtleAlg,
            true, key.key_ops ?: listOf(KeyUsage.Verify))
        val rawSignature = Base64Url.decode(signature.signature, logger = logger)
        val rawData = stringToByteArray(data)
        print("DATA ")
        printBytes(rawData)
        return subtle.verify(subtleAlg, cryptoKey, rawSignature, rawData)
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return byteArrayToString(Base64Url.decode(this.payload, logger = logger))
    }

}