package com.microsoft.portableIdentity.sdk.identifier

import com.google.crypto.tink.subtle.Hex
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierToken
import com.microsoft.portableIdentity.sdk.identifier.document.*
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentifierDocumentService
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.*
import java.security.MessageDigest
import kotlin.experimental.and
import kotlin.random.Random

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier constructor(
    val document: IdentifierDocumentPayload,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val alias: String,
    private val cryptoOperations: CryptoOperations,
    private val logger: ILogger,
    private val resolver: IResolver,
    private val registrar: IRegistrar
) {
    companion object {
        suspend fun createLongFormIdentifier(
            alias: String,
            cryptoOperations: CryptoOperations,
            logger: ILogger,
            signatureKeyReference: String,
            encryptionKeyReference: String,
            recoveryKeyReference: String,
            resolver: IResolver,
            registrar: IRegistrar
        ): IdentifierResponse {
            // TODO: Use software generated keys from the seed
//        val seed = cryptoOperations.generateSeed()
//        val publicKey = cryptoOperations.generatePairwise(seed)
            logger.debug("Creating identifier ($alias)")
            val personaEncKeyRef = "$alias.$encryptionKeyReference"
            val personaSigKeyRef = "$alias.$signatureKeyReference"
            val personaRecKeyRef = "$alias.$recoveryKeyReference"
            val signingKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
            val signingKeyJWK = signingKey.toJWK()
            val recoveryKey = cryptoOperations.generateKeyPair(personaRecKeyRef, KeyType.EllipticCurve)
            val recoveryKeyJWK = recoveryKey.toJWK()

            val identifierDocumentPayload: IdentifierDocumentPayload = IdentifierDocumentPayload(
                publicKeys = listOf(
                    IdentifierDocumentPublicKey(
                        id = signingKeyJWK.kid!!,
                        type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                        publicKeyHex = convertCryptoKeyToCompressedHex(Base64.decode(signingKeyJWK.x!!, logger), Base64.decode(signingKeyJWK.y!!, logger))
                    )
                )/*,
                services = listOf(IdentityHubService(
                    id = "#hubEndpoint",
                    publicKey = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                    serviceEndpoint = "https://beta.hub.microsoft.com/"
                    )
                )*/
            )

            val identifierDocumentPatch = IdentifierDocumentPatch("replace", identifierDocumentPayload)
            val updateCommitmentHashEncoded = generateCommitmentHash(logger)

            val patchData = PatchData(updateCommitmentHashEncoded, listOf(identifierDocumentPatch))
            val patchDataJson = Serializer.stringify(PatchData.serializer(), patchData)
            val patchDataByteArray = stringToByteArray(patchDataJson)
            val patchDataHash = byteArrayOf(18, 32)+hash(patchDataByteArray)
            val patchDataHashEncoded = Base64Url.encode(patchDataHash, logger)

            val patchDataEncoded = encodePatchData(patchData, logger)
            val recoveryCommitmentHashEncoded = generateCommitmentHash(logger)

            val suffixData = SuffixData(
                patchDataHashEncoded,
                RecoveryKey(convertCryptoKeyToCompressedHex(Base64.decode(recoveryKeyJWK.x!!, logger), Base64.decode(recoveryKeyJWK.y!!, logger))),
                recoveryCommitmentHashEncoded
            )
            val uniqueSuffix = computeUniqueSuffix(suffixData, logger)
            val portableIdentity = "did:ion:test:$uniqueSuffix"

            val suffixDataEncoded = encodeSuffixData(suffixData, logger)
            val registrationDocument = RegistrationDocument("create", suffixDataEncoded, patchDataEncoded)
            val registrationDocumentEncoded = encodeRegDoc(registrationDocument, logger)

            val identifierDocument = resolver.resolve(portableIdentity, registrationDocumentEncoded, cryptoOperations)
            logger.debug("Registered new decentralized identity")
            identifierDocument.alias = alias
            identifierDocument.signatureKeyReference = personaSigKeyRef
            return identifierDocument
        }

        private fun hash(bytes: ByteArray): ByteArray {
            val md = MessageDigest.getInstance("SHA-256")
            return md.digest(bytes)
        }

        private fun generateCommitmentHash(logger: ILogger): String {
            val commitmentValue = Base64Url.encode(Random.Default.nextBytes(32), logger)
            val commitmentValueHash = byteArrayOf(18, 32)+hash(stringToByteArray(commitmentValue))
            return Base64Url.encode(commitmentValueHash, logger)
        }

        private fun computeUniqueSuffix(suffixData: SuffixData, logger: ILogger): String {
            val suffixDataJson = Serializer.stringify(SuffixData.serializer(), suffixData)
            val suffixDataHash = byteArrayOf(18, 32)+hash(stringToByteArray(suffixDataJson))
            return Base64Url.encode(suffixDataHash, logger)
        }

        private fun encodeRegDoc(registrationDocument: RegistrationDocument, logger: ILogger): String{
            val regDocJson = Serializer.stringify(RegistrationDocument.serializer(), registrationDocument)
            return Base64Url.encode(stringToByteArray(regDocJson), logger)
        }

        private fun encodeSuffixData(suffixData: SuffixData, logger: ILogger): String {
            val suffixDataJson = Serializer.stringify(SuffixData.serializer(), suffixData)
            return Base64Url.encode(stringToByteArray(suffixDataJson), logger)
        }

        private fun encodePatchData(patchData: PatchData, logger: ILogger): String {
            val patchDataJson = Serializer.stringify(PatchData.serializer(), patchData)
            val patchDataByteArray = stringToByteArray(patchDataJson)
            return Base64Url.encode(patchDataByteArray, logger)
        }

        private fun convertCryptoKeyToCompressedHex(ecKeyX: ByteArray, ecKeyY: ByteArray): String {
            var yForCompressedHex = "0"+(2 + (ecKeyY[ecKeyY.size - 1] and 1)).toString()
            val xForCompressedHex = Hex.encode(ecKeyX)
            return """$yForCompressedHex$xForCompressedHex"""
        }
    }

    fun serialize(): String {
        return IdentifierToken.serialize(this)
    }

}
