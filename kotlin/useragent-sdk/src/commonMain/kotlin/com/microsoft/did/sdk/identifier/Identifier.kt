package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.document.service.IdentityHubUserService
import com.microsoft.did.sdk.registrars.IRegistrar
import com.microsoft.did.sdk.registrars.RegistrationDocument

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
                 private val signatureKeyReference: String,
                 private val encryptionKeyReference: String,
                 private val cryptoOperations: CryptoOperations,
                 private val resolver: IResolver,
                 private val registrar: IRegistrar) {
    companion object {
        suspend fun createAndRegister(
            alias: String,
            cryptoOperations: CryptoOperations,
            signatureKeyReference: String,
            encryptionKeyReference: String,
            resolver: IResolver,
            registrar: IRegistrar,
            identityHubDid: List<String>? = null
            ): Identifier {
            // TODO: Use software generated keys from the seed
//        val seed = cryptoOperations.generateSeed()
//        val publicKey = cryptoOperations.generatePairwise(seed)
            // prepending "a." for forward compatability with multi-persona sdk support
            val personaEncKeyRef = "$alias.$encryptionKeyReference"
            val personaSigKeyRef = "$alias.$signatureKeyReference"
            val encKey = cryptoOperations.generateKeyPair(personaEncKeyRef, KeyType.RSA)
            val sigKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
            var encJwk = encKey.toJWK()
            var sigJwk = sigKey.toJWK()
            encJwk.kid = "#${encJwk.kid}"
            sigJwk.kid = "#${encJwk.kid}"
            // RSA key
            val encPubKey = IdentifierDocumentPublicKey(
                id = encJwk.kid!!,
                type = "RsaVerificationKey2018",
                publicKeyJwk = encJwk
            )
            // Secp256k1 key
            val sigPubKey = IdentifierDocumentPublicKey(
                id = sigJwk.kid!!,
                type = "EcdsaSecp256k1VerificationKey2019",
                publicKeyJwk = sigJwk
            )
            var hubService: IdentityHubUserService? = null
            if (!identityHubDid.isNullOrEmpty()) {
                val hubs = identityHubDid.map {
                    resolver.resolve(it,
                    cryptoOperations
                )}
                val hubService = IdentityHubUserService.create(
                    id = "#hub",
                    keyStore = cryptoOperations.keyStore,
                    signatureKeyRef = personaSigKeyRef,
                    instances = hubs
                )
            }

            val document = RegistrationDocument(
                publicKeys = listOf(encPubKey, sigPubKey),
                services = if (hubService != null) {listOf(hubService)} else {
                    emptyList()}
            )
            val registered = registrar.register(document, personaSigKeyRef, cryptoOperations)
            return Identifier(
                document = registered,
                signatureKeyReference = personaSigKeyRef,
                encryptionKeyReference = personaEncKeyRef,
                cryptoOperations = cryptoOperations,
                resolver = resolver,
                registrar = registrar
            )
        }
    }
}