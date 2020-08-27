/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.toKeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.Constants.MASTER_IDENTIFIER_NAME
import com.microsoft.did.sdk.util.Constants.RECOVERY_KEYREFERENCE
import com.microsoft.did.sdk.util.Constants.SIGNATURE_KEYREFERENCE
import com.microsoft.did.sdk.util.Constants.UPDATE_KEYREFERENCE
import com.microsoft.did.sdk.util.controlflow.IdentifierCreatorException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.stringToByteArray
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Creates Identifiers
 * @class
 */
@Singleton
class IdentifierCreator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val payloadProcessor: SidetreePayloadProcessor
) {

    fun create(methodName: String): Result<Identifier> {
        val signatureKeyReference = "${SIGNATURE_KEYREFERENCE}_$methodName"
        val recoveryKeyReference = "${RECOVERY_KEYREFERENCE}_$methodName"
        val updateKeyReference = "${UPDATE_KEYREFERENCE}_$methodName"
        return try {
            val alias = Base64Url.encode(Random.nextBytes(2))
            val signingPublicKey = cryptoOperations.generateKeyPair("${alias}_$signatureKeyReference", KeyType.EllipticCurve)
            val recoveryPublicKey = cryptoOperations.generateKeyPair("${alias}_$recoveryKeyReference", KeyType.EllipticCurve)
            val updatePublicKey = cryptoOperations.generateKeyPair("${alias}_$updateKeyReference", KeyType.EllipticCurve)
            val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKey, recoveryPublicKey, updatePublicKey)
            val identifierLongForm = computeLongFormIdentifier(payloadProcessor, registrationPayload)

            Result.Success(
                transformIdentifierDocumentToIdentifier(
                    identifierLongForm,
                    alias,
                    signatureKeyReference,
                    recoveryKeyReference,
                    updateKeyReference,
                    MASTER_IDENTIFIER_NAME
                )
            )
        } catch (exception: Exception) {
            Result.Failure(IdentifierCreatorException("Unable to create an identifier", exception))
        }
    }

    fun createPairwiseId(personaId: String, peerId: String): Result<Identifier> {
        //TODO: Add a utility class/method to hash all the details identifying a pairwise id's key and use the hash as the key reference/kid
        val randomValueForKeyReference = Base64Url.encode(Random.nextBytes(6))
        val signatureKeyReference = "${SIGNATURE_KEYREFERENCE}_$randomValueForKeyReference"
        val recoveryKeyReference = "${RECOVERY_KEYREFERENCE}_$randomValueForKeyReference"
        val updateKeyReference = "${UPDATE_KEYREFERENCE}_$randomValueForKeyReference"
        return try {
            val alias = Base64Url.encode(Random.nextBytes(2))
            val alg = EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value
            )
            //TODO: Update the last section to append incremented version number instead of 1
            val signingKeyIdForPairwiseKey = "${signatureKeyReference}_1"
            val recoveryKeyIdForPairwiseKey = "${recoveryKeyReference}_1"
            val updateKeyIdForPairwiseKey = "${updateKeyReference}_1"
            val signingPublicKey =
                generateAndSaveKey(
                    alg,
                    peerId,
                    "${alias}_$signingKeyIdForPairwiseKey",
                    "${alias}_$signatureKeyReference",
                    personaId,
                    KeyUse.Signature.value
                )
            val recoveryPublicKey =
                generateAndSaveKey(alg, peerId, "${alias}_$recoveryKeyIdForPairwiseKey", "${alias}_$recoveryKeyReference", personaId, "")
            val updatePublicKey =
                generateAndSaveKey(alg, peerId, "${alias}_$updateKeyIdForPairwiseKey", "${alias}_$updateKeyReference", personaId, "")
            val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKey, recoveryPublicKey, updatePublicKey)
            val identifierLongForm = computeLongFormIdentifier(payloadProcessor, registrationPayload)

            Result.Success(
                transformIdentifierDocumentToIdentifier(
                    identifierLongForm,
                    alias,
                    signatureKeyReference,
                    recoveryKeyReference,
                    updateKeyReference,
                    pairwiseIdentifierName(peerId)
                )
            )
        } catch (exception: Exception) {
            Result.Failure(IdentifierCreatorException("Unable to create an identifier", exception))
        }
    }

    private fun generateAndSaveKey(
        algorithm: Algorithm,
        target: String,
        kid: String,
        keyReference: String?,
        personaId: String,
        keyUsage: String
    ): PublicKey {
        val privateKeyJwk = cryptoOperations.generatePairwise(algorithm, AndroidConstants.masterSeed.value, personaId, target)
        privateKeyJwk.kid = "#${kid}"
        privateKeyJwk.use = toKeyUse(keyUsage)
        val publicKeyJwk = privateKeyJwk.getPublicKey()
        publicKeyJwk.kid = "#${kid}"
        val pairwiseKeyReference = keyReference ?: generateKeyReferenceId(personaId, target, algorithm.name, KeyUse.Signature.value)
        cryptoOperations.keyStore.save(pairwiseKeyReference, privateKeyJwk)
        cryptoOperations.keyStore.getPrivateKey(pairwiseKeyReference)
        return publicKeyJwk
    }

    private fun generateKeyReferenceId(personaId: String, target: String, algorithm: String, keyType: String): String {
        return "$personaId-$target-$algorithm-$keyType"
    }

    /**
     * Computes unique suffix for did short form.
     * In unpublished resolution or long form, id is generated in SDK.
     */
    private fun computeUniqueSuffix(payloadProcessor: SidetreePayloadProcessor, registrationPayload: RegistrationPayload): String {
        val suffixDataByteArray = Base64Url.decode(registrationPayload.suffixData)
        val suffixDataHash = payloadProcessor.multiHash(suffixDataByteArray)
        val uniqueSuffix = Base64Url.encode(suffixDataHash)
        return "did:${Constants.METHOD_NAME}:$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(
        payloadProcessor: SidetreePayloadProcessor,
        registrationPayload: RegistrationPayload
    ): String {
        val registrationPayloadEncoded = registrationPayload.suffixData + "." + registrationPayload.patchData
        val identifierShortForm = computeUniqueSuffix(payloadProcessor, registrationPayload)
        return "$identifierShortForm?${Constants.INITIAL_STATE_LONGFORM}=$registrationPayloadEncoded"
    }

    private fun transformIdentifierDocumentToIdentifier(
        identifierLongForm: String,
        alias: String,
        signatureKeyReference: String,
        recoveryKeyReference: String,
        updateKeyReference: String,
        name: String
    ): Identifier {

        val personaSigningKeyRef = "${alias}_$signatureKeyReference"
        val personaRecoveryKeyRef = "${alias}_$recoveryKeyReference"
        val personaUpdateKeyRef = "${alias}_$updateKeyReference"

        return Identifier(
            identifierLongForm,
            alias,
            personaSigningKeyRef,
            "",
            personaRecoveryKeyRef,
            personaUpdateKeyRef,
            name
        )
    }

    private fun pairwiseIdentifierName(peerId: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64Url.encode(digest.digest(stringToByteArray(peerId)))
    }
}