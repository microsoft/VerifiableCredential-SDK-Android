/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.protocols.jose

/**
 * Class for JOSE constants
 */
enum class JoseConstants(val value: String) {

    /**
     * Define JOSE protocol name
     */
    Jose("JOSE"),

    /**
     * Define JWE protocol name
     */
    Jwe("JWE"),

    /**
     * Define JWS protocol name
     */
    Jws("JWS"),

    /**
     * Define JOSE algorithm constants
     */
    RsaOaep256("RSA-OAEP-256"),

    /**
     * Define JOSE algorithm constants
     */
    RsaOaep("RSA-OAEP"),

    /**
     * Define JOSE algorithm constants
     */
    Rs256("RS256"),

    /**
     * Define JOSE algorithm constants
     */
    Rs384("RS384"),

    /**
     * Define JOSE algorithm constants
     */
    Rs512("RS512"),

    /**
     * Define JOSE algorithm constants
     */
    Es256K("ES256K"),

    EcDsa("ECDSA"),

    EdDsa("EDDSA"),

    /**
     * Define JOSE algorithm constants
     */
    AesGcm128("128GCM"),

    /**
     * Define JOSE algorithm constants
     */
    AesGcm192("192GCM"),

    /**
     * Define JOSE algorithm constants
     */
    AesGcm256("256GCM"),

    /**
     * Define JOSE algorithm constants
     */
    Hs256("HS256"),

    /**
     * Define JOSE algorithm constants
     */
    Sha256("HA-256"),

    /**
     * Define JOSE algorithm constants
     */
    Hs512("HS512"),

    /**
     * Define the default signing algorithm
     */
    DefaultSigningAlgorithm(JoseConstants.Es256K.value),

    /**
     * Define the JOSE protocol elements
     */
    Alg("alg"),

    /**
     * Define the JOSE protocol elements
     */
    Kid("kid"),

    /**
     * Define the JOSE protocol elements
     */
    Type("typ"),

    /**
     * Define the JOSE protocol elements
     */
    Enc("enc"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenProtected("protected"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenUnprotected("unprotected"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenAad("aad"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenIv("iv"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenCiphertext("ciphertext"),

    /**
     * Define elements in the JWS Crypto Token
     */
    tokenTag("tag"),

    /**
     * Define elements in the JWE Crypto Token
     */
    tokenRecipients("recipients"),

    /**
     * Define elements in the JWS Crypto Token
     */
    tokenPayload("payload"),

    /**
     * Define elements in the JWS Crypto Token
     */
    tokenSignatures("signatures"),

    /**
     * Define elements in the JWS Crypto Token
     */
    tokenSignature("signature"),

    /**
     * Define elements in the JWS Crypto Token
     */
    tokenFormat("format"),

    /**
     * Define elements in the JOSE options
     */
    optionProtectedHeader("ProtectedHeader"),

    /**
     * Define elements in the JOSE options
     */
    optionHeader("Header"),

    /**
     * Define elements in the JOSE options
     */
    optionKidPrefix("KidPrefix"),

    /**
     * Define elements in the JOSE options
     */
    optionContentEncryptionAlgorithm("ContentEncryptionAlgorithm"),

    /**
     * Define JOSE serialization formats
     */
    serializationJwsFlatJson("JwsFlatJson"),

    /**
     * Define JOSE serialization formats
     */
    serializationJweFlatJson("JweFlatJson"),

    /**
     * Define JOSE serialization formats
     */
    serializationJwsGeneralJson("JwsGeneralJson"),

    /**
     * Define JOSE serialization formats
     */
    serializationJweGeneralJson("JweGeneralJson");

}