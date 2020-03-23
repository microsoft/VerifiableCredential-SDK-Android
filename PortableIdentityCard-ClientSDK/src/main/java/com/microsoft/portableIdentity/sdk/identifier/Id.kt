package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.identifier.document.*
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdHubService
import com.microsoft.portableIdentity.sdk.identifier.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.identifier.document.service.UserHubEndpoint
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.registrars.RegDoc
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDoc
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import java.security.MessageDigest

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Id constructor(
    val document: IdentifierDoc,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val alias: String,
    private val cryptoOperations: CryptoOperations,
    private val logger: ILogger,
    private val resolver: IResolver,
    private val registrar: IRegistrar
) {
    companion object {

        var microsoftIdentityHubDocument: IdentifierDoc = IdentifierDoc(
//            context = "https://w3id.org/did/v1",
//            id = "did:ion:test:EiD0fhJIYZwBNn2akeiVC5hT1K9ncP0HJCN0LkhnFrHyTg",
            publicKeys = listOf(
                IdentifierDocPublicKey(
                    id = "#key1",
                    type = "Secp256k1VerificationKey2018",
                    usage = "Signing",
                    publicKeyHex = "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
                )
            ),
            services = listOf(
                IdHubService(
                    id = "IdentityHub",
                    publicKey = "#key1",
                    serviceEndpoint = UserHubEndpoint(listOf("did:bar:456", "did:zaz:789"))
                )
            )
        )

        suspend fun createAndRegister(
            alias: String,
            cryptoOperations: CryptoOperations,
            logger: ILogger,
            signatureKeyReference: String,
            encryptionKeyReference: String,
            resolver: IResolver,
            registrar: IRegistrar,
            identityHubDid: List<String>? = null
        ): Id {
            // TODO: Use software generated keys from the seed
//        val seed = cryptoOperations.generateSeed()
//        val publicKey = cryptoOperations.generatePairwise(seed)
            logger.debug("Creating identifier ($alias)")
            val personaEncKeyRef = "$alias.$encryptionKeyReference"
            val personaSigKeyRef = "$alias.$signatureKeyReference"
            val encKey = cryptoOperations.generateKeyPair(personaEncKeyRef, KeyType.RSA)
            val sigKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
            val encJwk = encKey.toJWK()
            val sigJwk = sigKey.toJWK()
            logger.debug("Created keys ${encJwk.kid} and ${sigJwk.kid}")
            // RSA key
            val encPubKey = IdentifierDocPublicKey(
                id = encJwk.kid!!,
                type = LinkedDataKeySpecification.RsaSignature2018.values.first(),
                usage = "Signing",
                publicKeyHex = ""
            )
            // Secp256k1 key
            val sigPubKey = IdentifierDocPublicKey(
                id = sigJwk.kid!!,
                type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
                usage = "Signing",
                publicKeyHex = ""
            )
            var hubService: IdentifierDocService? = null
            if (!identityHubDid.isNullOrEmpty()) {
//                        val hubs = identityHubDid.map {
//                            resolver.resolve(it,
//                                cryptoOperations
//                            )}
                logger.debug("Adding Microsoft Identity Hub")
                val microsoftHub = Id(microsoftIdentityHubDocument, "", "", "", cryptoOperations, logger, resolver, registrar)
                hubService = IdHubService.create(
                    id = "#hub",
                    keyStore = cryptoOperations.keyStore,
                    signatureKeyRef = personaEncKeyRef,
                    instances = listOf(microsoftHub),
                    logger = logger
                )
            }

            val document = RegistrationDoc(
                context = "https://w3id.org/did/v1",
                publicKeys = listOf(encPubKey, sigPubKey),
                services = if (hubService != null) {
                    listOf(hubService)
                } else {
                    null
                }
            )
            val nextUpdateOtp = otpGenerator()
            val operationData = OperationData(hash(nextUpdateOtp), microsoftIdentityHubDocument)
            val nextRecoveryOtp = otpGenerator()
            val suffixData = SuffixData(
                hash(operationData.toString()),
                RecoveryKey("03f513461b26cfeb508c79ae884f1090e8e431d06bbc6ae52eea31fd381bc52fa5"),
                hash(nextRecoveryOtp)
            )

            val regDoc = RegDoc("create", "", "")
            val registered = registrar.register(
                regDoc, personaSigKeyRef, cryptoOperations)

            logger.debug("Registered new decentralized identity")
            return Id(
                alias = alias,
                document = registered,
                signatureKeyReference = personaSigKeyRef,
                encryptionKeyReference = personaEncKeyRef,
                cryptoOperations = cryptoOperations,
                logger = logger,
                resolver = resolver,
                registrar = registrar
            )
        }

        fun otpGenerator(): String {
            return randomAlphanumericString()
        }

        fun randomAlphanumericString(): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val outputStrLength = (1..28).shuffled().first()

            return (1..outputStrLength)
                .map { kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }

        fun hash(string: String): String {
            val bytes = string.toString().toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("", { str, it -> str + "%02x".format(it) })
        }
    }

    fun serialize(): String {
        return IdToken.serialize(this)
    }

}
