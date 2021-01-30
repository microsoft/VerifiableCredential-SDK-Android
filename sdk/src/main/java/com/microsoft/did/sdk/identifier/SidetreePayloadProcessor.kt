package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.identifier.models.identifierdocument.LinkedDataKeySpecification
import com.microsoft.did.sdk.identifier.models.payload.IdentifierDocumentPatch
import com.microsoft.did.sdk.identifier.models.payload.PatchData
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPublicKeyInput
import com.microsoft.did.sdk.util.Constants.IDENTIFIER_PUBLIC_KEY_PURPOSE
import com.microsoft.did.sdk.util.Constants.SIDETREE_PATCH_ACTION
import com.microsoft.did.sdk.util.SideTreeHelper
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SidetreePayloadProcessor @Inject constructor(
    private val sideTreeHelper: SideTreeHelper,
    private val serializer: Json
) {
    /**
     * Generates input payload for create operation on Sidetree.
     * In unpublished resolution or long form it is same as the initial-state portion of the identifier which can be used
     * to resolve portable identifier
     */
    fun generateCreatePayload(
        signingPublicKey: JWK,
        recoveryPublicKey: JWK,
        updatePublicKey: JWK
    ): RegistrationPayload {
        val identifierDocumentPatch = createIdentifierDocumentPatch(signingPublicKey)
        val patchData = createPatchData(identifierDocumentPatch, generatePublicKeyJwk(updatePublicKey))
        val suffixData = createSuffixData(patchData, generatePublicKeyJwk(recoveryPublicKey))
        return RegistrationPayload(suffixData, patchData)
    }

    private fun generatePublicKeyJwk(publicKeyJwk: JWK): JWK {
        return publicKeyJwk
        // TODO: verify the params kty, crv, x, y are properly set on this JWK, otherwise apply below code:
//        return when (publicKeyJwk.keyType) {
//            KeyType.RSA.value -> publicKeyJwk
//            else -> {
//                //Sidetree api specifically checks for curve name for elliptic curve keys. Hence it is set here.
//                val curveName = SECP256K1_CURVE_NAME_EC
//                JsonWebKey(kty = publicKeyJwk.kty, crv = curveName, x = publicKeyJwk.x, y = publicKeyJwk.y)
//            }
//        }
    }

    private fun createDocumentPayload(signingPublicKey: JWK): IdentifierDocumentPayload {
        return IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKeyInput(
                    id = signingPublicKey.keyID,
                    type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                    publicKeyJwk = generatePublicKeyJwk(signingPublicKey),
                    purpose = listOf(IDENTIFIER_PUBLIC_KEY_PURPOSE)
                )
            )
        )
    }

    private fun createIdentifierDocumentPatch(signingPublicKey: JWK): IdentifierDocumentPatch {
        val identifierDocumentPayload = createDocumentPayload(signingPublicKey)
        return IdentifierDocumentPatch(SIDETREE_PATCH_ACTION, identifierDocumentPayload)
    }

    private fun createPatchData(identifierDocumentPatch: IdentifierDocumentPatch, updatePublicKey: JWK): PatchData {
        val updateCommitment = sideTreeHelper.createCommitmentValue(updatePublicKey)
        return PatchData(updateCommitment, listOf(identifierDocumentPatch))
    }

    private fun createSuffixData(patchData: PatchData, recoveryKey: JWK): SuffixData {
        val patchDataJson = serializer.encodeToString(PatchData.serializer(), patchData)
        val patchDataEncoded = sideTreeHelper.canonicalizeAndMultiHash(patchDataJson)
        val recoveryCommitment = sideTreeHelper.createCommitmentValue(recoveryKey)

        return SuffixData(patchDataEncoded, recoveryCommitment)
    }
}