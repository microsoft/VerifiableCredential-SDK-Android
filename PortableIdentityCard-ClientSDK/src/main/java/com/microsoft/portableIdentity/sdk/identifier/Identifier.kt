// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.portableIdentity.sdk.identifier.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier constructor (
                 val document: IdentifierDocument,
                 val signatureKeyReference: String,
                 val encryptionKeyReference: String,
                 val alias: String,
                 private val cryptoOperations: CryptoOperations,
                 private val resolver: IResolver,
                 private val registrar: IRegistrar,
                 private val serializer: Serializer) {
    companion object {

        private val microsoftIdentityHubDocument = IdentifierDocument(
            context = "https://w3id.org/did/v1",
            id = "did:test:hub.id",
            created = "2019-07-15T22:36:00.881Z",
            publicKeys = listOf(
                IdentifierDocumentPublicKey(
                    id = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                    type = "RsaVerificationKey2018",
                    controller = "did:test:hub.id",
                    publicKeyJwk = JsonWebKey(
                        kty = "RSA",
                        kid = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                        alg = "RSA-OAEP",
                        key_ops = listOf("sign", "verify", "wrapKey", "unwrapKey", "encrypt", "decrypt"),
                        n = "uG76CgQGPSTx0ZuJBvof4ceNj4Taci3xaFpt_2hQeLhbjvE_N7SHFU86rFWxZMv_DP7h9cfDImp" +
                                "imbUpg3tmcd5jTsulwGHSQr4u1WfQXqN_BiGJ9EyGhIYTjPNBXODpZCsO62GksLlJi1xaZU" +
                                "_EobC98s3sUsdI_zkjnuTL2T2ar3kzP8Pj0WkSRf-2WE1gXLNW8fzB8Y7_gFPtdwuTx4EYH" +
                                "MEeuqZhzjPBtuw7PLrCbYm3EHx5BCNIhJag3cyDLMOHmp4xlof9_zNZQ5UpxOlJuRHNgz9o" +
                                "nthtm2fYS_R-ZBZH2JNhAkUsMHQFF5GAISAMkG877HOupBhRRn6VQybHqeVyzqfgKKpCHni" +
                                "ZACAZTp5zy5GhGVnik4qZcrSvZMLGscftz71zqV-ny9Ck5WIJ6gSGoGDwigJx3smt_seyYM" +
                                "xJUJjYF3NGzmzLALZwMWq4FNu21iBFMovzpb5aCcC-HQhVFyLSzkZS2-AEM-7TE0MMeWQcj" +
                                "pJCmOxgl0zrf7MFv5IDlco_hO4WRmFp9NIqewLDrS52fdN_yjnH3mKwnJYByomHhOnMNTTg" +
                                "oqrVOZzO59mOycz0Mx4rKTxyWcDwUrO8wb846m11JL06I-D5i7KBrQpHy8E0Yeabr5gWkdR" +
                                "rAc_9Ifox5vJ3lZzkBYHYq871xneyURPh9LZqP2E",
                        e = "AQAB"
                    ))),
            services = listOf(IdentityHubService(
                id = "#hubEndpoint",
                publicKey = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                serviceEndpoint = ServiceHubEndpoint(listOf("https://beta.hub.microsoft.com/"))
            ))
        )


        // TODO: needs refactoring! Dependency inject this object instead of having this companion etc.
        suspend fun createAndRegister(
            alias: String,
            cryptoOperations: CryptoOperations,
            signatureKeyReference: String,
            encryptionKeyReference: String,
            resolver: IResolver,
            registrar: IRegistrar,
            serializer: Serializer,
            identityHubDid: List<String>? = null
            ): Identifier {
            // TODO: Use software generated keys from the seed
//        val seed = cryptoOperations.generateSeed()
//        val publicKey = cryptoOperations.generatePairwise(seed)
            SdkLog.d("Creating identifier ($alias)")
            val personaEncKeyRef = "$alias.$encryptionKeyReference"
            val personaSigKeyRef = "$alias.$signatureKeyReference"
            val encKey = cryptoOperations.generateKeyPair(personaEncKeyRef, KeyType.RSA)
            val sigKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
            val encJwk = encKey.toJWK()
            val sigJwk = sigKey.toJWK()
            SdkLog.d("Created keys ${encJwk.kid} and ${sigJwk.kid}") // TODO(get rid)
            // RSA key
            val encPubKey = IdentifierDocumentPublicKey(
                id = encJwk.kid!!,
                type =  LinkedDataKeySpecification.RsaSignature2018.values.first(),
                publicKeyJwk = encJwk
            )
            // Secp256k1 key
            val sigPubKey = IdentifierDocumentPublicKey(
                id = sigJwk.kid!!,
                type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                publicKeyJwk = sigJwk
            )
            var hubService: IdentifierDocumentService? = null
            if (!identityHubDid.isNullOrEmpty()) {
//                        val hubs = identityHubDid.map {
//                            resolver.resolve(it,
//                                cryptoOperations
//                            )}
                SdkLog.d("Adding Microsoft Identity Hub")
                val microsoftHub = Identifier(microsoftIdentityHubDocument, "", "", "", cryptoOperations, resolver, registrar, serializer)
                hubService = IdentityHubService.create(
                    id = "#hub",
                    keyStore = cryptoOperations.keyStore,
                    signatureKeyRef = personaEncKeyRef,
                    instances = listOf(microsoftHub)
                )
            }

            val document = RegistrationDocument(
                context = "https://w3id.org/did/v1",
                publicKeys = listOf(encPubKey, sigPubKey),
                services = if (hubService != null) {listOf(hubService)} else { null }
            )

            val registered = registrar.register(document, personaSigKeyRef, cryptoOperations)

            SdkLog.d("Registered new decentralized identity")
            return Identifier(
                alias = alias,
                document = registered,
                signatureKeyReference = personaSigKeyRef,
                encryptionKeyReference = personaEncKeyRef,
                cryptoOperations = cryptoOperations,
                resolver = resolver,
                registrar = registrar,
                serializer = serializer
            )
        }
    }

    fun serialize(): String {
        return IdentifierToken.serialize(this, serializer)
    }
}