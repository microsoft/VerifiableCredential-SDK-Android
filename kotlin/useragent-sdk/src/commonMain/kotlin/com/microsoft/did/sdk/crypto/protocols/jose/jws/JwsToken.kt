package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStoreListItem
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.byteArrayToString
import com.microsoft.did.sdk.utilities.stringToByteArray
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify

/**
 * Class for containing JWS token operations.
 * @class
 */
class JwsToken private constructor(private val payload: String, signatures: List<JwsSignature> = emptyList()) {

    private val signatures: MutableList<JwsSignature> = signatures.toMutableList()

    companion object {
        fun deserialize(jws: String): JwsToken {
            val compactRegex = Regex("([A-Za-z_\\-]*)\\.([A-Za-z_\\-]*)\\.([A-Za-z_\\-]*)")
            val compactMatches = compactRegex.matchEntire(jws.trim())
            if (compactMatches != null) {
                // compact JWS format
                val protected = compactMatches.groupValues[0]
                val payload = compactMatches.groupValues[1]
                val signature = compactMatches.groupValues[2]
                val jwsSignatureObject = JwsSignature(
                    protected = protected,
                    header =  null,
                    signature = signature
                )
                return JwsToken(payload, listOf(jwsSignatureObject))
            } else if (jws.toLowerCase().contains("\"signatures\"")) { // check for signature or signatures
                // GENERAL
                val token = Json.parse(JwsGeneralJson.serializer(), jws)
                return JwsToken(
                    payload = token.payload,
                    signatures = token.signatures
                )
            } else if (jws.toLowerCase().contains("\"signature\"")) {
                // Flat
                val token = Json.parse(JwsFlatJson.serializer(), jws)
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
                Json.stringify(JwsCompact.serializer(), jws)
            }
            JwsFormat.FlatJson -> {
                val jws = intermediateFlatJsonSerialize()
                Json.stringify(JwsFlatJson.serializer(), jws)
            }
            JwsFormat.GeneralJson -> {
                val jws = intermediateGeneralJsonSerialize()
            Json.stringify(JwsGeneralJson.serializer(), jws)
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
        val signingKey = cryptoOperations.keyStore.getPublicKey(signingKeyReference).getKey()

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
            if (signingKey.kid != null) {
                protected[JoseConstants.Kid.value] = signingKey.kid!!
            }
        }

        var encodedProtected = ""
        if (protected.count() > 0) {
            val jsonProtected = Json.stringify(protected)
            encodedProtected = Base64Url.encode(stringToByteArray(jsonProtected))
        }

        val signatureInput = "$encodedProtected.${this.payload}"

        val signature = cryptoOperations.sign(
            stringToByteArray(signatureInput), signingKeyReference,
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
    fun verify(cryptoOperations: CryptoOperations) {
        val keyStoreKeys = cryptoOperations.keyStore.list()
        val aliasList = keyStoreKeys.values.map { listItem: KeyStoreListItem -> listItem.kids }.reduce {
            acc, curr ->
            acc.addAll(curr)
            acc
        }
        this.signatures.forEach {
            val kid = it.getKid()
            val signatureInput = "${it.protected}.${this.payload}"
            val signature = Base64Url.decode(it.signature)
            if (aliasList.contains(kid)) {
                // we can perform this verification using our own keys
                val key = (keyStoreKeys.entries.filter { key -> key.value.kids.contains(kid) }).first()
                cryptoOperations.verify(stringToByteArray(signatureInput), signature, key.key)
            } else {
                // we must retrieve the associated DID
                TODO("Resolver must be implemented to get key $kid")
            }
        }
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return byteArrayToString(Base64Url.decode(this.payload))
    }

}