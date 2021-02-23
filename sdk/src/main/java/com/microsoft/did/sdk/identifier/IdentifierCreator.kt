/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.identifier

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.did.sdk.crypto.MacAlgorithm
import com.microsoft.did.sdk.crypto.PrivateKeyFactoryAlgorithm
import com.microsoft.did.sdk.crypto.PublicKeyFactoryAlgorithm
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.AES_KEY
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import org.erdtman.jcs.JsonCanonicalizer
import java.security.KeyPair
import java.security.MessageDigest
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentifierCreator @Inject constructor(
    private val payloadProcessor: SidetreePayloadProcessor,
    private val sideTreeHelper: SideTreeHelper,
    private val serializer: Json,
    private val keyStore: EncryptedKeyStore
) {

    fun create(personaName: String): Identifier {
        val signingPublicKeyJwk = generateAndStoreKeyPair()
        val recoveryPublicKeyJwk = generateAndStoreKeyPair()
        val updatePublicKeyJwk = generateAndStoreKeyPair()

        return createIdentifier(personaName, signingPublicKeyJwk, recoveryPublicKeyJwk, updatePublicKeyJwk)
    }

    private fun createIdentifier(
        personaName: String,
        signingPublicKey: JWK,
        recoveryPublicKey: JWK,
        updatePublicKey: JWK
    ): Identifier {
        val registrationPayload = payloadProcessor.generateCreatePayload(signingPublicKey, recoveryPublicKey, updatePublicKey)
        val identifierLongForm = computeLongFormIdentifier(registrationPayload)

        return Identifier(
            identifierLongForm,
            signingPublicKey.keyID,
            "",
            recoveryPublicKey.keyID,
            updatePublicKey.keyID,
            personaName
        )
    }

    /**
     * Generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun generateAndStoreKeyPair(): JWK {
        val keyId = generateRandomKeyId()
        val privateKey = CryptoOperations.generateKeyPair(KeyGenAlgorithm.Secp256k1).toPrivateJwk(keyId)
        keyStore.storeKey(keyId, privateKey)
        return privateKey.toPublicJWK()
    }

    fun generatePersonaSeed(personaDid: String): ByteArray {
        val masterSeed = keyStore.getKey(Constants.MAIN_IDENTIFIER_REFERENCE).toOctetSequenceKey().toByteArray()
        return CryptoOperations.computeMac(personaDid.toByteArray(), SecretKeySpec(masterSeed, AES_KEY), MacAlgorithm.HmacSha512)
    }

    private fun generateRandomKeyId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun createPairwiseId(persona: Identifier, peerId: String): Identifier {
        val pairwisePersonaName = pairwiseIdentifierName(persona.id, peerId)
        val signingPublicKeyJwk = createAndStorePairwiseKeyPair(persona, peerId)
        val recoveryPublicKeyJwk = createAndStorePairwiseKeyPair(persona, peerId)
        val updatePublicKeyJwk = createAndStorePairwiseKeyPair(persona, peerId)

        return createIdentifier(pairwisePersonaName, signingPublicKeyJwk, recoveryPublicKeyJwk, updatePublicKeyJwk)
    }

    /**
     * Creates a new pairwise KeyPair from given key material and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun createAndStorePairwiseKeyPair(persona: Identifier, peerId: String): JWK {
        val keyId = generateRandomKeyId()
        val pairwisePrivateKey = createPairwiseKeyPair(persona, peerId).toPrivateJwk(keyId)
        keyStore.storeKey(keyId, pairwisePrivateKey)
        return pairwisePrivateKey.toPublicJWK()
    }

    private fun createPairwiseKeyPair(persona: Identifier, peerId: String): KeyPair {
        val privateKeySpec = EcPairwisePrivateKeySpec(
            generatePersonaSeed(persona.id),
            peerId
        )
        val privateKey = CryptoOperations.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(privateKeySpec))
        val publicKeySpec = EcPairwisePublicKeySpec(privateKey)
        val publicKey = CryptoOperations.generateKey<ECPublicKey>(PublicKeyFactoryAlgorithm.EcPairwise(publicKeySpec))
        return KeyPair(publicKey, privateKey)
    }

    private fun computeDidShortFormIdentifier(registrationPayload: RegistrationPayload): String {
        val suffixDataString = serializer.encodeToString(SuffixData.serializer(), registrationPayload.suffixData)
        val uniqueSuffix = sideTreeHelper.canonicalizeMultiHashEncode(suffixDataString)
        return "did${Constants.COLON}${Constants.METHOD_NAME}${Constants.COLON}$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(registrationPayload: RegistrationPayload): String {
        val registrationPayloadString = serializer.encodeToString(RegistrationPayload.serializer(), registrationPayload)
        val registrationPayloadCanonicalized = JsonCanonicalizer(registrationPayloadString).encodedUTF8
        val registrationPayloadCanonicalizedEncoded = Base64.encodeToString(registrationPayloadCanonicalized, Constants.BASE64_URL_SAFE)
        val identifierShortForm = computeDidShortFormIdentifier(registrationPayload)
        return "$identifierShortForm${Constants.COLON}$registrationPayloadCanonicalizedEncoded"
    }

    fun pairwiseIdentifierName(personaDid: String, peerId: String): String {
        val concatDids = personaDid + peerId
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64.encodeToString(digest.digest(concatDids.toByteArray()), Constants.BASE64_URL_SAFE)
    }
}