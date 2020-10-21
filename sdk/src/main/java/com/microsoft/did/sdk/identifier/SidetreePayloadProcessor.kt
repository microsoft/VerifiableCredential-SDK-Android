package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.identifier.models.identifierdocument.LinkedDataKeySpecification
import com.microsoft.did.sdk.identifier.models.payload.IdentifierDocumentPatch
import com.microsoft.did.sdk.identifier.models.payload.PatchData
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPublicKeyInput
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants.SECP256K1_CURVE_NAME_EC
import com.microsoft.did.sdk.util.Constants.SIDETREE_MULTIHASH_CODE
import com.microsoft.did.sdk.util.Constants.SIDETREE_MULTIHASH_LENGTH
import com.microsoft.did.sdk.util.Constants.SIDETREE_PATCH_ACTION
import com.microsoft.did.sdk.util.stringToByteArray
import kotlinx.serialization.json.Json
import org.erdtman.jcs.JsonCanonicalizer
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SidetreePayloadProcessor @Inject constructor(private val serializer: Json) {
    /**
     * Generates input payload for create operation on Sidetree.
     * In unpublished resolution or long form it is same as the initial-state portion of the identifier which can be used
     * to resolve portable identifier
     */
    fun generateCreatePayload(
        signingPublicKey: PublicKey,
        recoveryPublicKey: PublicKey,
        updatePublicKey: PublicKey
    ): RegistrationPayload {
        //Generates key pair for signing and encryption. Recovery key is required to recover portable identifier on Sidetree
        val signingKeyJWK = signingPublicKey.toJWK()
        val recoveryKeyJWK = recoveryPublicKey.toJWK()
        val updateKeyJWK = updatePublicKey.toJWK()

        return generateRegistrationPayload(signingKeyJWK, recoveryKeyJWK, updateKeyJWK)
    }

    private fun generateRegistrationPayload(
        signingKeyJWK: JsonWebKey,
        recoveryKeyJWK: JsonWebKey,
        updateKeyJWK: JsonWebKey
    ): RegistrationPayload {
        val identifierDocumentPatch = createIdentifierDocumentPatch(signingKeyJWK)
        val patchData = createPatchData(identifierDocumentPatch, generatePublicKeyJwk(updateKeyJWK))
        val patchDataEncoded = encodePatchData(patchData)

        val suffixData = createSuffixData(patchData, generatePublicKeyJwk(recoveryKeyJWK))
        val suffixDataEncoded = encodeSuffixData(suffixData)
        return RegistrationPayload(suffixDataEncoded, patchDataEncoded)
    }

    private fun generatePublicKeyJwk(publicKeyJwk: JsonWebKey): JsonWebKey {
        return when (publicKeyJwk.kty) {
            KeyType.RSA.value -> publicKeyJwk
            else -> {
                //Sidetree api specifically checks for curve name for elliptic curve keys. Hence it is set here.
                val curveName = SECP256K1_CURVE_NAME_EC
                JsonWebKey(kty = publicKeyJwk.kty, crv = curveName, x = publicKeyJwk.x, y = publicKeyJwk.y)
            }
        }
    }

    private fun createDocumentPayload(signingKeyJWK: JsonWebKey): IdentifierDocumentPayload {
        return IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKeyInput(
                    id = signingKeyJWK.kid!!.substringAfter('#'),
                    type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                    jwk = generatePublicKeyJwk(signingKeyJWK),
                    purpose = listOf("auth", "general")
                )
            )
        )
    }

    private fun createIdentifierDocumentPatch(signingKeyJWK: JsonWebKey): IdentifierDocumentPatch {
        val identifierDocumentPayload = createDocumentPayload(signingKeyJWK)
        return IdentifierDocumentPatch(SIDETREE_PATCH_ACTION, identifierDocumentPayload)
    }

    private fun createPatchData(identifierDocumentPatch: IdentifierDocumentPatch, updateKeyJWK: JsonWebKey): PatchData {
        //Generates hash of commit-reveal value which would be used while requesting next update operation on Sidetree
        val updateCommitmentCanonicalized = canonicalizePublicKeyAsByteArray(updateKeyJWK)
        val updateCommitmentHash = multiHash(updateCommitmentCanonicalized)
        val updateCommitmentHashEncoded = Base64Url.encode(updateCommitmentHash)
        return PatchData(updateCommitmentHashEncoded, listOf(identifierDocumentPatch))
    }

    private fun createSuffixDataPayload(patchData: PatchData, recoveryCommitmentHash: ByteArray): SuffixData {
        val patchDataJson = serializer.encodeToString(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        val patchDataHash = multiHash(patchDataByteArray)
        val patchDataHashEncoded = Base64Url.encode(patchDataHash)

        val recoveryCommitmentHashEncoded = Base64Url.encode(recoveryCommitmentHash)

        return SuffixData(patchDataHashEncoded, recoveryCommitmentHashEncoded)
    }

    private fun createSuffixData(patchData: PatchData, recoveryKeyJWK: JsonWebKey): SuffixData {
        //Generates hash of commit-reveal value which would be used while requesting recovery on Sidetree
        val recoveryCommitmentCanonicalized = canonicalizePublicKeyAsByteArray(recoveryKeyJWK)
        val recoveryCommitmentHash = multiHash(recoveryCommitmentCanonicalized)
        return createSuffixDataPayload(patchData, recoveryCommitmentHash)
    }

    internal fun multiHash(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        //Prepend the hash value with hash algorithm code and digest length to be in multihash format as expected by Sidetree
        return byteArrayOf(SIDETREE_MULTIHASH_CODE.toByte(), SIDETREE_MULTIHASH_LENGTH.toByte()) + digest.digest(bytes)
    }

    private fun encodeSuffixData(suffixData: SuffixData): String {
        val suffixDataJson = serializer.encodeToString(SuffixData.serializer(), suffixData)
        val suffixDataByteArray = stringToByteArray(suffixDataJson)
        return Base64Url.encode(suffixDataByteArray)
    }

    private fun encodePatchData(patchData: PatchData): String {
        val patchDataJson = serializer.encodeToString(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        return Base64Url.encode(patchDataByteArray)
    }

    internal fun canonicalizePublicKeyAsByteArray(publicKeyJwk: JsonWebKey): ByteArray {
        val commitmentValue = serializer.encodeToString(JsonWebKey.serializer(), publicKeyJwk)
        val jsonCanonicalizer = JsonCanonicalizer(commitmentValue)
        return jsonCanonicalizer.encodedUTF8
    }
}