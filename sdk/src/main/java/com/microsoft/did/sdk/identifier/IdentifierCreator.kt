/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.identifier

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.KeyAlgorithm
import com.microsoft.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.spi.EcPairwiseKeySpec
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.stringToByteArray
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class IdentifierCreator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val payloadProcessor: SidetreePayloadProcessor,
    private val keyStore: EncryptedKeyStore,
    private val serializer: Json
) {

    fun create(name: String): Identifier {
        val signingPublicKeyJwk = generateKeyPair()
        val recoveryPublicKeyJwk = generateKeyPair()
        val updatePublicKeyJwk = generateKeyPair()

        val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKeyJwk, recoveryPublicKeyJwk, updatePublicKeyJwk)
        val identifierLongForm = computeLongFormIdentifier(registrationPayload)

        return Identifier(
            identifierLongForm,
            signingPublicKeyJwk.keyID,
            "",
            recoveryPublicKeyJwk.keyID,
            updatePublicKeyJwk.keyID,
            name
        )
    }

    /**
     * Generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun generateKeyPair(): JWK {
        val keyId = generateRandomKeyId()
        cryptoOperations.generateKeyPair(keyId, KeyGenAlgorithm.Secp256k1())
        return JWK.load(keyStore.keyStore, keyId, null).toPublicJWK()
    }

    private fun generateRandomKeyId(): String {
        return Base64.encodeToString(Random.nextBytes(16), Base64.URL_SAFE)
    }

    fun createPairwiseId(persona: Identifier, peerId: String): Identifier {
        //TODO: Add a utility class/method to hash all the details identifying a pairwise id's key and use the hash as the key reference/kid
        val randomValueForKeyReference = Base64.encodeToString(Random.nextBytes(6), Base64.URL_SAFE)
        val signatureKeyReference = "${SIGNATURE_KEYREFERENCE}_$randomValueForKeyReference"
        val recoveryKeyReference = "${RECOVERY_KEYREFERENCE}_$randomValueForKeyReference"
        val updateKeyReference = "${UPDATE_KEYREFERENCE}_$randomValueForKeyReference"
        val alias = Base64.encodeToString(Random.nextBytes(2), Base64.URL_SAFE)
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
        val identifierLongForm = computeLongFormIdentifier(registrationPayload)

        return Identifier(
            identifierLongForm,
            signatureKeyReference,
            "",
            recoveryKeyReference,
            updateKeyReference,
            pairwiseIdentifierName(peerId)
        )
    }

    private fun generateAndSaveKey(
        algorithm: Algorithm,
        peerDid: String,
        kid: String,
        keyReference: String?,
        personaDid: String,
        keyUsage: String
    ): PublicKey {
        val privateKeyJwk = cryptoOperations.generatePairwise(algorithm, AndroidConstants.masterSeed.value, personaDid, peerDid)
        val pairwiseKeySpec = EcPairwiseKeySpec("SEED TODO", personaDid, peerDid)
        cryptoOperations.generatePrivateKey(KeyAlgorithm.EcPairwise(pairwiseKeySpec))


        privateKeyJwk.kid = "#${kid}"
        privateKeyJwk.use = KeyUse.fromString(keyUsage)
        val publicKeyJwk = privateKeyJwk.getPublicKey()
        publicKeyJwk.kid = "#${kid}"
        val pairwiseKeyReference = keyReference ?: generateKeyReferenceId(personaDid, peerDid, algorithm.name, KeyUse.Signature.value)
        cryptoOperations.keyStore.saveKey(pairwiseKeyReference, privateKeyJwk)
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
    private fun computeUniqueSuffix(registrationPayload: RegistrationPayload): String {
        val suffixDataCanonicalized =
            canonicalizeToByteArray(serializer.encodeToString(SuffixData.serializer(), registrationPayload.suffixData))
        val suffixDataCanonicalizedHash = multiHash(suffixDataCanonicalized)
        val uniqueSuffix = Base64.encodeToString(suffixDataCanonicalizedHash, Base64.URL_SAFE)
        return "did${Constants.COLON}${Constants.METHOD_NAME}${Constants.COLON}$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(registrationPayload: RegistrationPayload): String {
        val registrationPayloadCanonicalized =
            canonicalizeToByteArray(serializer.encodeToString(RegistrationPayload.serializer(), registrationPayload))
        val registrationPayloadCanonicalizedEncoded = Base64.encodeToString(registrationPayloadCanonicalized, Base64.URL_SAFE)
        val identifierShortForm = computeUniqueSuffix(registrationPayload)
        return "$identifierShortForm${Constants.COLON}$registrationPayloadCanonicalizedEncoded"
    }

    private fun pairwiseIdentifierName(peerId: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64.encodeToString(digest.digest(stringToByteArray(peerId)), Base64.URL_SAFE)
    }
}