/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.AndroidConstants
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.toKeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.EcKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.portableIdentity.sdk.utilities.Constants.IDENTIFIER_SECRET_KEY_NAME
import com.microsoft.portableIdentity.sdk.utilities.Constants.RECOVERY_KEYREFERENCE
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIGNATURE_KEYREFERENCE
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IdentifierCreatorException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Creates Identifiers
 * @class
 */
@Singleton
class IdentifierCreator @Inject constructor(private val cryptoOperations: CryptoOperations,
                                            private val payloadProcessor: SidetreePayloadProcessor) {

    fun create(methodName: String): Result<Identifier> {
        val signatureKeyReference = "${SIGNATURE_KEYREFERENCE}_$methodName"
        val recoveryKeyReference = "${RECOVERY_KEYREFERENCE}_$methodName"
        return try {
            val alias = Base64Url.encode(Random.nextBytes(2))
            val signingPublicKey = cryptoOperations.generateKeyPair("${alias}_$signatureKeyReference", KeyType.EllipticCurve)
            val recoveryPublicKey = cryptoOperations.generateKeyPair("${alias}_$recoveryKeyReference", KeyType.EllipticCurve)
            val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKey, recoveryPublicKey)
            val identifierLongForm = computeLongFormIdentifier(payloadProcessor, registrationPayload)

            Result.Success(
                transformIdentifierDocumentToIdentifier(
                    payloadProcessor,
                    registrationPayload,
                    identifierLongForm,
                    alias,
                    signatureKeyReference,
                    recoveryKeyReference,
                    IDENTIFIER_SECRET_KEY_NAME
                )
            )
        } catch (exception: Exception) {
            Result.Failure(IdentifierCreatorException("Unable to create an identifier", exception))
        }
    }

    fun createPairwiseId(personaId:String, peerId: String): Result<Identifier> {
        //TODO: Add a utility class/method to hash all the details identifying a pairwise id's key and use the hash as the key reference/kid
        val randomValueForKeyReference = Base64Url.encode( Random.nextBytes(6))
        val signatureKeyReference = "${SIGNATURE_KEYREFERENCE}_$randomValueForKeyReference"
        val recoveryKeyReference = "${RECOVERY_KEYREFERENCE}_$randomValueForKeyReference"
        return try {
            val alias = Base64Url.encode(Random.nextBytes(2))
            val alg = EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.Sha256
                )
            )
            val keys = cryptoOperations.keyStore.list()
            //TODO: Update the last section to append incremented version number instead of 1
            val signingKeyIdForPairwiseKey = "${signatureKeyReference}_1"
            val recoveryKeyIdForPairwiseKey = "${recoveryKeyReference}_1"
            val signingPublicKey = generateAndSaveKey(alg, peerId, "${alias}_$signingKeyIdForPairwiseKey","${alias}_$signatureKeyReference", personaId, KeyUse.Signature.value)
            val recoveryPublicKey = generateAndSaveKey(alg, peerId, "${alias}_$recoveryKeyIdForPairwiseKey","${alias}_$recoveryKeyReference", personaId, "")
            val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKey, recoveryPublicKey)
            val identifierLongForm = computeLongFormIdentifier(payloadProcessor, registrationPayload)

            Result.Success(
                transformIdentifierDocumentToIdentifier(
                    payloadProcessor,
                    registrationPayload,
                    identifierLongForm,
                    alias,
                    signatureKeyReference,
                    recoveryKeyReference,
                    pairwiseIdentifierName(peerId)
                )
            )
        } catch (exception: Exception) {
            Result.Failure(IdentifierCreatorException("Unable to create an identifier", exception))
        }
    }

    private fun generateAndSaveKey(algorithm: Algorithm, target: String, kid: String, keyReference: String?, personaId: String, keyUsage: String) : PublicKey {
        val privateKeyJwk = cryptoOperations.generatePairwise(algorithm, AndroidConstants.masterSeed.value, personaId, target)
        privateKeyJwk.kid = kid
        privateKeyJwk.use = toKeyUse(keyUsage)
        val publicKeyJwk = privateKeyJwk.getPublicKey()
        publicKeyJwk.kid = kid
        val pairwiseKeyReference = keyReference ?: generateKeyReferenceId(personaId, target, algorithm.name, KeyUse.Signature.value)
        cryptoOperations.keyStore.save(pairwiseKeyReference, privateKeyJwk)
        return publicKeyJwk
    }

    private fun generateKeyReferenceId(personaId: String, target: String, algorithm: String, keyType: String) : String {
        return "$personaId-$target-$algorithm-$keyType"
    }

    private fun computeUniqueSuffix(payloadProcessor: SidetreePayloadProcessor, registrationPayload: RegistrationPayload): String {
        val uniqueSuffix = payloadProcessor.computeUniqueSuffix(registrationPayload.suffixData)
        return "did:${Constants.METHOD_NAME}:$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(
        payloadProcessor: SidetreePayloadProcessor,
        registrationPayload: RegistrationPayload
    ): String {
        val registrationPayloadEncoded = registrationPayload.suffixData+"."+registrationPayload.patchData
        val identifierShortForm = computeUniqueSuffix(payloadProcessor, registrationPayload)
        return "$identifierShortForm?${Constants.INITIAL_STATE_LONGFORM}=$registrationPayloadEncoded"
    }

    private fun transformIdentifierDocumentToIdentifier(
        payloadProcessor: SidetreePayloadProcessor,
        registrationPayload: RegistrationPayload,
        identifierLongForm: String,
        alias: String,
        signatureKeyReference: String,
        recoveryKeyReference: String,
        name: String
    ): Identifier {
        val nextUpdateCommitmentHash = payloadProcessor.extractNextUpdateCommitmentHash(registrationPayload)
        val nextRecoveryCommitmentHash = payloadProcessor.extractNextRecoveryCommitmentHash(registrationPayload)

        val personaSigKeyRef = "${alias}_$signatureKeyReference"
        val personaRecKeyRef = "${alias}_$recoveryKeyReference"

        return Identifier(
            identifierLongForm,
            alias,
            personaSigKeyRef,
            "",
            personaRecKeyRef,
            nextUpdateCommitmentHash,
            nextRecoveryCommitmentHash,
            name
        )
    }

    private fun pairwiseIdentifierName(peerId: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64Url.encode(digest.digest(stringToByteArray(peerId)))
    }
}