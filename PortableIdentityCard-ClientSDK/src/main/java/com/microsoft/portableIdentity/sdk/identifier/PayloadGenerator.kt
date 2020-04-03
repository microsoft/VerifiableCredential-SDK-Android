package com.microsoft.portableIdentity.sdk.identifier

import com.google.crypto.tink.subtle.Hex
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.models.PatchData
import com.microsoft.portableIdentity.sdk.identifier.models.SuffixData
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPatch
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPayload
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPublicKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.RecoveryKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument
import com.microsoft.portableIdentity.sdk.utilities.*
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.experimental.and
import kotlin.random.Random

@Singleton
class PayloadGenerator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String,
    @Named("encryptionKeyReference") private val encryptionKeyReference: String,
    @Named("recoveryKeyReference") private val recoveryKeyReference: String
) {
    fun generateCreatePayload(alias: String):String {
        val personaEncKeyRef = "$alias.$encryptionKeyReference"
        val personaSigKeyRef = "$alias.$signatureKeyReference"
        val personaRecKeyRef = "$alias.$recoveryKeyReference"
        val signingKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
        val signingKeyJWK = signingKey.toJWK()
        val encryptionKey = cryptoOperations.generateKeyPair(personaEncKeyRef, KeyType.RSA)
        val encryptionKeyJWK = encryptionKey.toJWK()
        val recoveryKey = cryptoOperations.generateKeyPair(personaRecKeyRef, KeyType.EllipticCurve)
        val recoveryKeyJWK = recoveryKey.toJWK()

        val identifierDocumentPatch = createIdentifierDocumentPatch(signingKeyJWK, encryptionKeyJWK)

        val updateCommitmentHash = generateCommitmentValue()
        val updateCommitmentHashEncoded = Base64Url.encode(updateCommitmentHash)
        val patchData = PatchData(
            updateCommitmentHashEncoded,
            listOf(identifierDocumentPatch)
        )

        val patchDataEncoded = encodePatchData(patchData)
        val recoveryCommitmentHash = generateCommitmentValue()
        val recoveryKeyHex = RecoveryKey(convertCryptoKeyToCompressedHex(Base64.decode(recoveryKeyJWK.x!!), Base64.decode(recoveryKeyJWK.y!!)))
        val suffixData = createSuffixDataPayload(patchData, recoveryCommitmentHash, recoveryKeyHex)

        val suffixDataEncoded = encodeSuffixData(suffixData)
        val registrationDocument = RegistrationDocument("create", suffixDataEncoded, patchDataEncoded)
        return encodeRegDoc(registrationDocument)
    }

    fun computeUniqueSuffix(suffixDataEncoded: String): String {
        val suffixDataDecoded = Base64Url.decode(suffixDataEncoded)
        val suffixDataJson = byteArrayToString(suffixDataDecoded)
        val suffixDataHash = byteArrayOf(18, 32)+hash(stringToByteArray(suffixDataJson))
        return Base64Url.encode(suffixDataHash)
    }

    private fun createDocumentPayload(signingKeyJWK: JsonWebKey, encryptionKeyJWK: JsonWebKey): IdentifierDocumentPayload {
        return IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKey(
                    id = "cIiCWr41",
                    type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                    publicKeyHex = convertCryptoKeyToCompressedHex(Base64.decode(signingKeyJWK.x!!), Base64.decode(signingKeyJWK.y!!))
                )
            ),
            serviceEndpoints = listOf(
                IdentityHubService(
                    id = "test",
                    serviceEndpoint = "https://beta.hub.microsoft.com"
                )
            )
        )
    }

    private fun createIdentifierDocumentPatch(signingKeyJWK: JsonWebKey, encryptionKeyJWK: JsonWebKey): IdentifierDocumentPatch {
        val identifierDocumentPayload = createDocumentPayload(signingKeyJWK, encryptionKeyJWK)
        return IdentifierDocumentPatch("replace", identifierDocumentPayload)
    }

    private fun createSuffixDataPayload(patchData: PatchData, recoveryCommitmentHash: ByteArray, recoveryKeyHex: RecoveryKey): SuffixData {
        val patchDataJson = Serializer.stringify(PatchData.serializer(), patchData)
        val patchDataByteArray = stringToByteArray(patchDataJson)
        val patchDataHash = byteArrayOf(18, 32)+ hash(patchDataByteArray)
        val patchDataHashEncoded = Base64Url.encode(patchDataHash)

        val recoveryCommitmentHashEncoded = Base64Url.encode(recoveryCommitmentHash)

        return SuffixData(
            patchDataHashEncoded,
            recoveryKeyHex,
            recoveryCommitmentHashEncoded
        )
    }

    private fun hash(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    private fun generateCommitmentValue(): ByteArray {
        val commitmentValue = Base64Url.encode(Random.Default.nextBytes(32))
        return byteArrayOf(18, 32)+ hash(stringToByteArray(commitmentValue))
    }

    private fun encodeRegDoc(registrationDocument: RegistrationDocument): String{
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

    private fun convertCryptoKeyToCompressedHex(ecKeyX: ByteArray, ecKeyY: ByteArray): String {
        var yForCompressedHex = "0"+(2 + (ecKeyY[ecKeyY.size - 1] and 1)).toString()
        val xForCompressedHex = Hex.encode(ecKeyX)
        return """$yForCompressedHex$xForCompressedHex"""
    }
}