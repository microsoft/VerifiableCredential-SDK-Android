package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.identifier.models.payload.PatchData
import com.microsoft.portableIdentity.sdk.identifier.models.payload.SuffixData
import com.microsoft.portableIdentity.sdk.identifier.models.payload.IdentifierDocumentPatch
import com.microsoft.portableIdentity.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import com.microsoft.portableIdentity.sdk.identifier.models.payload.document.IdentifierDocumentPublicKeyInput
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIDETREE_CURVE_NAME_FOR_EC
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIDETREE_MULTIHASH_CODE
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIDETREE_MULTIHASH_LENGTH
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIDETREE_OPERATION_TYPE
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIDETREE_PATCH_ACTION
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.byteArrayToString
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SidetreePayloadProcessor @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String,
    @Named("recoveryKeyReference") private val recoveryKeyReference: String,
    private val serializer: Serializer
) {
    /**
     * Generates input payload for create operation on Sidetree.
     * In unpublished resolution or long form it is same as the initial-state portion of the identifier which can be used
     * to resolve portable identifier
     */
    fun generateCreatePayload(alias: String): RegistrationPayload {
        //Generates key pair for signing and encryption. Recovery key is required to recover portable identifier on Sidetree
        val signingPublicKey = cryptoOperations.generateKeyPair("$alias" + "_" + "$signatureKeyReference", KeyType.EllipticCurve)
        val recoveryPublicKey = cryptoOperations.generateKeyPair("$alias" + "_" + "$recoveryKeyReference", KeyType.EllipticCurve)
        val signingKeyJWK = signingPublicKey.toJWK()
        val recoveryKeyJWK = recoveryPublicKey.toJWK()

        return generateRegistrationPayload(signingKeyJWK, recoveryKeyJWK)
    }

    /**
     * Computes unique suffix for did short form.
     * In unpublished resolution or long form, id is generated in SDK.
     */
    fun computeUniqueSuffix(suffixDataEncoded: String): String {
        val suffixDataHash = hash(stringToByteArray(suffixDataEncoded))
        return Base64Url.encode(suffixDataHash)
    }

    fun extractNextUpdateCommitmentHash(registrationPayload: RegistrationPayload): String {
        val patchDataJson = byteArrayToString(Base64Url.decode(registrationPayload.patchData))
        return serializer.parse(PatchData.serializer(), patchDataJson).nextUpdateCommitmentHash
    }

    fun extractNextRecoveryCommitmentHash(registrationPayload: RegistrationPayload): String {
        val suffixDataJson = byteArrayToString(Base64Url.decode(registrationPayload.suffixData))
        return serializer.parse(SuffixData.serializer(), suffixDataJson).nextRecoveryCommitmentHash
    }

    private fun generateRegistrationPayload(signingKeyJWK: JsonWebKey, recoveryKeyJWK: JsonWebKey): RegistrationPayload {
        val identifierDocumentPatch = createIdentifierDocumentPatch(signingKeyJWK)
        val patchData = createPatchData(identifierDocumentPatch)
        val patchDataEncoded = encodePatchData(patchData)

        val suffixDataEncoded = createSuffixDataEncoded(patchData, generatePublicKeyJwk(recoveryKeyJWK))
        return RegistrationPayload(/*SIDETREE_OPERATION_TYPE,*/ suffixDataEncoded, patchDataEncoded)
    }

    private fun generatePublicKeyJwk(publicKeyJwk: JsonWebKey): JsonWebKey {
        return when (publicKeyJwk.kty) {
            KeyType.RSA.value -> publicKeyJwk
            else -> {
                //Sidetree api specifically checks for curve name for elliptic curve keys. Hence it is set here.
                val curveName = SIDETREE_CURVE_NAME_FOR_EC
                JsonWebKey(kty = publicKeyJwk.kty, crv = curveName, x = publicKeyJwk.x, y = publicKeyJwk.y)
            }
        }
    }

    private fun createDocumentPayload(signingKeyJWK: JsonWebKey): IdentifierDocumentPayload {
        return IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKeyInput(
                    //TODO: Look into new restrictions on Sidetree api for id length(20) and characters allowed(only base64url charsets)
                    id = signingKeyJWK.kid!!,
//                    id = "testkey",
                    type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                    jwk = generatePublicKeyJwk(signingKeyJWK),
                    usage = listOf("ops", "auth", "general")
                )
            )
        )
    }

    private fun createIdentifierDocumentPatch(signingKeyJWK: JsonWebKey): IdentifierDocumentPatch {
        val identifierDocumentPayload = createDocumentPayload(signingKeyJWK)
        return IdentifierDocumentPatch(SIDETREE_PATCH_ACTION, identifierDocumentPayload)
    }

    private fun createPatchData(identifierDocumentPatch: IdentifierDocumentPatch): PatchData {
        //Generates hash of commit-reveal value which would be used while requesting next update operation on Sidetree
        val updateCommitmentHash = generateCommitmentValue()
        val updateCommitmentHashEncoded = Base64Url.encode(updateCommitmentHash)
        return PatchData(updateCommitmentHashEncoded, listOf(identifierDocumentPatch))
    }

    private fun createSuffixDataPayload(patchData: PatchData, recoveryCommitmentHash: ByteArray, recoveryKeyJWK: JsonWebKey): SuffixData {
        val patchDataJson = serializer.stringify(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        val patchDataHash = hash(patchDataByteArray)
        val patchDataHashEncoded = Base64Url.encode(patchDataHash)

        val recoveryCommitmentHashEncoded = Base64Url.encode(recoveryCommitmentHash)

        return SuffixData(patchDataHashEncoded, recoveryKeyJWK, recoveryCommitmentHashEncoded)
    }

    private fun createSuffixDataEncoded(patchData: PatchData, recoveryKeyJWK: JsonWebKey): String {
        //Generates hash of commit-reveal value which would be used while requesting recovery on Sidetree
        val recoveryCommitmentHash = generateCommitmentValue()
        val suffixData = createSuffixDataPayload(patchData, recoveryCommitmentHash, recoveryKeyJWK)
        return encodeSuffixData(suffixData)
    }

    private fun hash(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        //Prepend the hash value with hash algorithm code and digest length to be in multihash format as expected by Sidetree
        return byteArrayOf(SIDETREE_MULTIHASH_CODE.toByte(), SIDETREE_MULTIHASH_LENGTH.toByte()) + digest.digest(bytes)
    }

    private fun generateCommitmentValue(): ByteArray {
        val commitmentValue = Base64Url.encode(Random.Default.nextBytes(32))
        return hash(stringToByteArray(commitmentValue))
    }

    private fun encodeSuffixData(suffixData: SuffixData): String {
        val suffixDataJson = serializer.stringify(SuffixData.serializer(), suffixData)
        return Base64Url.encode(stringToByteArray(suffixDataJson))
    }

    private fun encodePatchData(patchData: PatchData): String {
        val patchDataJson = serializer.stringify(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        return Base64Url.encode(patchDataByteArray)
    }
}