/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.utilities.ILogger

object CryptoHelpers {
//    /**
//     * The API which implements the requested algorithm
//     * @param cryptoFactory Crypto suite
//     * @param algorithmName Requested algorithm
//     * @param hash Optional hash for the algorithm
//     */
//    fun getSubtleCryptoForAlgorithm(cryptoFactory: CryptoFactory, algorithm: Algorithm): SubtleCrypto {
//        const jwa = CryptoHelpers.webCryptoToJwa(algorithm)
//        switch (algorithm.name.toUpperCase()) {
//            case 'RSASSA-PKCS1-V1_5':
//            case 'ECDSA':
//            return cryptoFactory.getMessageSigner(jwa);
//            case 'RSA-OAEP':
//            case 'RSA-OAEP-256':
//            return cryptoFactory.getKeyEncrypter(jwa);
//            case 'AES-GCM':
//            return cryptoFactory.getSymmetricEncrypter(jwa);
//            case 'HMAC':
//            return cryptoFactory.getMessageAuthenticationCodeSigners(jwa);
//            case 'SHA-256':
//            case 'SHA-384':
//            case 'SHA-512':
//            return cryptoFactory.getMessageDigest(jwa);
//        }
//
//        throw new Error(`Algorithm '${MinimalJson.serializer.stringify(algorithm)}' is not supported`);
//    }

    /**
     * Map the JWA algorithm to the W3C crypto API algorithm.
     * The method restricts the supported algorithms. This can easily be extended.
     * Based on https://www.w3.org/TR/WebCryptoAPI/ A. Mapping between JSON Web Key / JSON Web Algorithm
     * @param jwaAlgorithmName Requested algorithm
     * @see https://www.w3.org/TR/WebCryptoAPI/#jwk-mapping
     */
    fun jwaToWebCrypto(jwa: String, vararg args: List<Any>, logger: ILogger): Algorithm {
        val regex = Regex("\\d+")
        return when (jwa.toUpperCase()) {
            JoseConstants.Rs256.value,
            JoseConstants.Rs384.value,
            JoseConstants.Rs512.value -> {
                val matches = regex.findAll(jwa)
                return Algorithm (
                    name = W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
                    additionalParams = mapOf(
                        "hash" to Sha.get(matches.first().value.toInt(), logger = logger)
                    )
                )
            }
            JoseConstants.RsaOaep.value, // According to the spec, this should point to SHA-1
            JoseConstants.RsaOaep256.value -> RsaOaepParams(
                additionalParams = mapOf(
                    "hash" to Sha.Sha256
                )
            )
            JoseConstants.AesGcm128.value,
            JoseConstants.AesGcm192.value,
            JoseConstants.AesGcm256.value -> {
                val iv = args[0] as ByteArray
                val aad = args[1] as ByteArray
                val matches = regex.findAll(jwa)
                val length = matches.first().value.toUShort()
                return AesGcmParams(
                    iv = iv,
                    additionalData = aad,
                    tagLength = 128.toByte(),
                    additionalParams = mapOf(
                        "length" to length
                    )
                )
            }
            JoseConstants.Es256K.value -> EcdsaParams(
                    hash =  Sha.Sha256,
                additionalParams = mapOf(
                    "namedCurve" to "P-256K",
                    "format" to "DER"
                )
            )
            else -> error("Algorithm $jwa is not supported")
        }
    }

//    /**
//     * Maps the subtle crypto algorithm name to the JWA name
//     * @param algorithmName Requested algorithm
//     * @param hash Optional hash for the algorithm
//     */
//    public static webCryptoToJwa(algorithm: any): string {
//        const hash = algorithm.hash || 'SHA-256';
//        switch (algorithm.name.toUpperCase()) {
//            case 'RSASSA-PKCS1-V1_5':
//            return `RS${CryptoHelpers.getHash(hash)}`;
//            case 'ECDSA':
//            return `ES256K`;
//            case 'RSA-OAEP-256':
//            return 'RSA-OAEP-256';
//            case 'RSA-OAEP':
//            return `RSA-OAEP-${CryptoHelpers.getHash(hash)}`;
//            case 'AES-GCM':
//            const length = algorithm.length || 128;
//            return `A${length}GCMKW`;
//
//            case 'HMAC':
//            return `HS${CryptoHelpers.getHash(hash)}`;
//
//            case 'SHA-256':
//            case 'SHA-384':
//            case 'SHA-512':
//            return `SHA${CryptoHelpers.getHash(hash)}`;
//        }
//
//        throw new Error(`Algorithm '${MinimalJson.serializer.stringify(algorithm)}' is not supported`);
//    }
//
//    /**
//     * Derive the key import algorithm
//     * @param algorithm used for signature
//     */
//    public static getKeyImportAlgorithm(algorithm: CryptoAlgorithm, jwk: PublicKey | JsonWebKey): string | RsaHashedImportParams | EcKeyImportParams | HmacImportParams | DhImportKeyParams {
//        const hash = (<any>algorithm).hash || 'SHA-256';
//        const name = algorithm.name;
//        switch (algorithm.name.toUpperCase()) {
//            case 'RSASSA-PKCS1-V1_5':
//            return  <RsaHashedImportParams>{ name, hash: {name: "SHA-256"} };
//            case 'HMAC':
//            case 'SHA-256':
//            case 'SHA-384':
//            case 'SHA-512':
//            return <RsaHashedImportParams>{ name, hash };
//            case 'ECDSA':
//            case 'ECDH':
//            return <EcKeyImportParams>{ name, namedCurve: (<EcPublicKey>jwk).crv };
//            case 'RSA-OAEP':
//            case 'RSA-OAEP-256':
//            return {name, hash: 'SHA-256'}
//            case 'AES-GCM':
//            return <RsaHashedImportParams>{ name };
//        }
//        throw new Error(`Algorithm '${MinimalJson.serializer.stringify(algorithm)}' is not supported`);
//    }
//
//    private static getHash(hash: any) {
//        if (hash.name) {
//            return (hash.name).toUpperCase().replace('SHA-', '');
//        }
//        return (hash || 'SHA-256').toUpperCase().replace('SHA-', '');
//    }
//
//    private static getRegexMatch(matches: RegExpExecArray, index: number): string {
//        return matches[index];
//    }
}