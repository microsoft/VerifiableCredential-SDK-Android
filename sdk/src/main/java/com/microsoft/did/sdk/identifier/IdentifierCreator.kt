/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.identifier

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.SideTreeHelper
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import org.erdtman.jcs.JsonCanonicalizer
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import java.security.KeyFactory
import java.security.KeyPair
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class IdentifierCreator @Inject constructor(
    private val payloadProcessor: SidetreePayloadProcessor,
    private val sideTreeHelper: SideTreeHelper,
    private val serializer: Json
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
        CryptoOperations.generateKeyPair(keyId, KeyGenAlgorithm.Secp256k1())
        return JWK.load(EncryptedKeyStore.keyStore, keyId, null).toPublicJWK()
    }

    private fun generateRandomKeyId(): String {
        return Base64.encodeToString(Random.nextBytes(16), Base64.URL_SAFE)
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
        val pairwiseKeys = createPairwiseKeyPair(persona, peerId)
        EncryptedKeyStore.storeKeyPair(pairwiseKeys, keyId)
        return JWK.load(EncryptedKeyStore.keyStore, keyId, null).toPublicJWK()
    }

    private fun createPairwiseKeyPair(persona: Identifier, peerId: String): KeyPair {
        val keyFactory = KeyFactory.getInstance("EcPairwise", "DID")
        val keySpec = EcPairwisePrivateKeySpec(
            CryptoOperations.getSeed(persona.name),
            persona.id,
            peerId
        )
        val privateKey = keyFactory.generatePrivate(keySpec) as ECPrivateKey
        val publicKeySpec = EcPairwisePublicKeySpec(privateKey)
        val publicKey = keyFactory.generatePublic(publicKeySpec) as ECPublicKey
        return KeyPair(publicKey, privateKey)
    }

    private fun computeDidShortFormIdentifier(registrationPayload: RegistrationPayload): String {
        val suffixDataString = serializer.encodeToString(SuffixData.serializer(), registrationPayload.suffixData)
        val uniqueSuffix = sideTreeHelper.canonicalizeAndMultiHash(suffixDataString)
        return "did${Constants.COLON}${Constants.METHOD_NAME}${Constants.COLON}$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(registrationPayload: RegistrationPayload): String {
        val registrationPayloadString = serializer.encodeToString(RegistrationPayload.serializer(), registrationPayload)
        val registrationPayloadCanonicalized = JsonCanonicalizer(registrationPayloadString).encodedUTF8
        val registrationPayloadCanonicalizedEncoded = Base64.encodeToString(registrationPayloadCanonicalized, Base64.URL_SAFE)
        val identifierShortForm = computeDidShortFormIdentifier(registrationPayload)
        return "$identifierShortForm${Constants.COLON}$registrationPayloadCanonicalizedEncoded"
    }

    private fun pairwiseIdentifierName(personaDid: String, peerId: String): String {
        val concatDids = personaDid + peerId
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64.encodeToString(digest.digest(concatDids.toByteArray()), Base64.URL_SAFE)
    }
}