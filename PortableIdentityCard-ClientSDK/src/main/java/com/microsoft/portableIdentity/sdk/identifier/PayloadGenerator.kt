package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.models.PatchData
import com.microsoft.portableIdentity.sdk.identifier.models.SuffixData
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPatch
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPayload
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPublicKeyInput
import com.microsoft.portableIdentity.sdk.identifier.models.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
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
class PayloadGenerator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String,
    @Named("encryptionKeyReference") private val encryptionKeyReference: String,
    @Named("recoveryKeyReference") private val recoveryKeyReference: String
) {
    /**
     * Generates input payload for create operation on Sidetree.
     * In unpublished resolution or long form it is same as the initial-state portion of the identifier which can be used
     * to resolve portable identifier
     */
    fun generateCreatePayload(alias: String): String {
        //Generates key pair for signing and encryption. Recovery key is required to recover portable identifier on Sidetree
        val personaSigKeyRef = "$alias.$signatureKeyReference"
        val personaEncKeyRef = "$alias.$encryptionKeyReference"
        val personaRecKeyRef = "$alias.$recoveryKeyReference"
        val signingKeyJWK = generatePublicKeyJwk(personaSigKeyRef, KeyType.EllipticCurve)
        //TODO: Do we need encryption keys for MVP?
        val encryptionKeyJWK = generatePublicKeyJwk(personaEncKeyRef, KeyType.RSA)
        val recoveryKeyJWK = generatePublicKeyJwk(personaRecKeyRef, KeyType.EllipticCurve)

        val identifierDocumentPatch = createIdentifierDocumentPatch(signingKeyJWK, encryptionKeyJWK)
        val patchData = createPatchData(identifierDocumentPatch)
        val patchDataEncoded = encodePatchData(patchData)

        val suffixDataEncoded = createSuffixDataEncoded(patchData, recoveryKeyJWK)

        val registrationDocument = RegistrationDocument(SIDETREE_OPERATION_TYPE, suffixDataEncoded, patchDataEncoded)
        return encodeRegDoc(registrationDocument)
    }

    private fun generatePublicKeyJwk(personaKeyRef: String, keyType: KeyType): JsonWebKey {
        val publicKey = cryptoOperations.generateKeyPair(personaKeyRef, keyType)
        val publicKeyJwk = publicKey.toJWK()
        return if (keyType == KeyType.RSA)
            publicKeyJwk
        else {
            var curveName = if (publicKeyJwk.kty == KeyType.EllipticCurve.value) "secp256k1" else publicKeyJwk.crv
            JsonWebKey(kty = publicKeyJwk.kty, crv = curveName, x = publicKeyJwk.x, y = publicKeyJwk.y)
        }
    }

    /**
     * Computes unique suffix for portable identifier.
     * In unpublished resolution or long form, id is generated in SDK.
     */
    fun computeUniqueSuffix(suffixDataEncoded: String): String {
        val suffixDataDecoded = Base64Url.decode(suffixDataEncoded)
        val suffixDataJson = byteArrayToString(suffixDataDecoded)
        val suffixDataHash = byteArrayOf(18, 32) + hash(stringToByteArray(suffixDataJson))
        return Base64Url.encode(suffixDataHash)
    }

    private fun createDocumentPayload(signingKeyJWK: JsonWebKey, encryptionKeyJWK: JsonWebKey): IdentifierDocumentPayload {
        return IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKeyInput(
                    //TODO: Look into new restrictions on Sidetree api for id length(20) and characters allowed(only base64url charsets)
                    /*id = signingKeyJWK.kid!!,*/
                    id = "testkey",
                    type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                    jwk = signingKeyJWK
                ),
                IdentifierDocumentPublicKeyInput(
                    //TODO: Look into new restrictions on Sidetree api for id length(20) and characters allowed(only base64url charsets)
                    /*id = signingKeyJWK.kid!!,*/
                    id = "testkeys",
                    type = LinkedDataKeySpecification.RsaSignature2018.values.first(),
                    jwk = encryptionKeyJWK
                )
            ),
            serviceEndpoints = listOf(
                IdentityHubService(
                    //TODO: What should be the default values for these while registering portable identity? Are we supporting these for MVP?
                    id = "test",
                    serviceEndpoint = "https://beta.hub.microsoft.com"
                )
            )
        )
    }

    private fun createIdentifierDocumentPatch(signingKeyJWK: JsonWebKey, encryptionKeyJWK: JsonWebKey): IdentifierDocumentPatch {
        val identifierDocumentPayload = createDocumentPayload(signingKeyJWK, encryptionKeyJWK)
        return IdentifierDocumentPatch(SIDETREE_PATCH_ACTION, identifierDocumentPayload)
    }

    private fun createPatchData(identifierDocumentPatch: IdentifierDocumentPatch): PatchData {
        //Generates hash of commit-reveal value which would be used while requesting next update operation on Sidetree
        val updateCommitmentHash = generateCommitmentValue()
        val updateCommitmentHashEncoded = Base64Url.encode(updateCommitmentHash)
        return PatchData(
            updateCommitmentHashEncoded,
            listOf(identifierDocumentPatch)
        )
    }

    private fun createSuffixDataPayload(patchData: PatchData, recoveryCommitmentHash: ByteArray, recoveryKeyJWK: JsonWebKey): SuffixData {
        val patchDataJson = Serializer.stringify(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        val patchDataHash = byteArrayOf(18, 32) + hash(patchDataByteArray)
        val patchDataHashEncoded = Base64Url.encode(patchDataHash)

        val recoveryCommitmentHashEncoded = Base64Url.encode(recoveryCommitmentHash)

        return SuffixData(
            patchDataHashEncoded,
            recoveryKeyJWK,
            recoveryCommitmentHashEncoded
        )
    }

    private fun createSuffixDataEncoded(patchData: PatchData, recoveryKeyJWK: JsonWebKey): String {
        //Generates hash of commit-reveal value which would be used while requesting recovery on Sidetree
        val recoveryCommitmentHash = generateCommitmentValue()
        val suffixData = createSuffixDataPayload(patchData, recoveryCommitmentHash, recoveryKeyJWK)
        return encodeSuffixData(suffixData)
    }

    private fun hash(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    private fun generateCommitmentValue(): ByteArray {
        val commitmentValue = Base64Url.encode(Random.Default.nextBytes(32))
        return byteArrayOf(18, 32) + hash(stringToByteArray(commitmentValue))
    }

    private fun encodeRegDoc(registrationDocument: RegistrationDocument): String {
        val regDocJson = Serializer.stringify(RegistrationDocument.serializer(), registrationDocument)
        return Base64Url.encode(stringToByteArray(regDocJson))
    }

    private fun encodeSuffixData(suffixData: SuffixData): String {
        val suffixDataJson = Serializer.stringify(SuffixData.serializer(), suffixData)
        return Base64Url.encode(stringToByteArray(suffixDataJson))
    }

    private fun encodePatchData(patchData: PatchData): String {
        val patchDataJson = Serializer.stringify(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        return Base64Url.encode(patchDataByteArray)
    }
}