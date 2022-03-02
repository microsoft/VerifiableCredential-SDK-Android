/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.did.sdk.identifier

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.did.sdk.crypto.MacAlgorithm
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.did.sdk.identifier.models.payload.SuffixData
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.AES_KEY
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.json.Json
import org.erdtman.jcs.JsonCanonicalizer
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
    private fun generateAndStoreKeyPair(use: KeyUse = KeyUse.SIGNATURE): JWK {
        val keyId = generateRandomKeyId()
        val privateKey = CryptoOperations.generateKeyPair(KeyGenAlgorithm.Secp256k1).toPrivateJwk(keyId, use)
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
}