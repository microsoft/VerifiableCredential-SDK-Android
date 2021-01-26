package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.identifier.models.payload.IdentifierDocumentPatch
import com.microsoft.did.sdk.identifier.models.payload.PatchData
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants.IDENTIFIER_PUBLIC_KEY_PURPOSE
import com.microsoft.did.sdk.util.Constants.SECP256K1_CURVE_NAME_EC
import com.microsoft.did.sdk.util.canonicalizeAsByteArray
import com.microsoft.did.sdk.util.multiHash
import com.microsoft.did.sdk.util.nonMultiHash
import kotlinx.serialization.json.Json
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

        val suffixData = createSuffixData(patchData, generatePublicKeyJwk(recoveryKeyJWK))
        return RegistrationPayload(suffixData, patchData)
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
                    publicKeyJwk = generatePublicKeyJwk(signingKeyJWK),
                    purpose = listOf(IDENTIFIER_PUBLIC_KEY_PURPOSE)
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
        val updateCommitmentCanonicalized =
            canonicalizeAsByteArray(serializer.encodeToString(JsonWebKey.serializer(), updateKeyJWK))
        val updateCommitmentHash = nonMultiHash(updateCommitmentCanonicalized)
        val updateCommitmentDoubleHash = multiHash(updateCommitmentHash)
        val updateCommitmentDoubleHashEncoded = Base64Url.encode(updateCommitmentDoubleHash)
        return PatchData(updateCommitmentDoubleHashEncoded, listOf(identifierDocumentPatch))
    }

    private fun createSuffixDataPayload(patchData: PatchData, recoveryCommitmentHash: ByteArray): SuffixData {
        val patchDataJson = serializer.encodeToString(PatchData.serializer(), patchData)
        val patchDataByteArray = canonicalizeAsByteArray(patchDataJson)
        val patchDataHash = multiHash(patchDataByteArray)
        val patchDataHashEncoded = Base64Url.encode(patchDataHash)

        val recoveryCommitmentHashEncoded = Base64Url.encode(recoveryCommitmentHash)

        return SuffixData(patchDataHashEncoded, recoveryCommitmentHashEncoded)
    }

    private fun createSuffixData(patchData: PatchData, recoveryKeyJWK: JsonWebKey): SuffixData {
        //Generates hash of commit-reveal value which would be used while requesting recovery on Sidetree
        val recoveryCommitmentCanonicalized =
            canonicalizeAsByteArray(serializer.encodeToString(JsonWebKey.serializer(), recoveryKeyJWK))
        val recoveryCommitmentHash = nonMultiHash(recoveryCommitmentCanonicalized)
        val recoveryCommitmentDoubleHash = multiHash(recoveryCommitmentHash)
        return createSuffixDataPayload(patchData, recoveryCommitmentDoubleHash)
    }
}