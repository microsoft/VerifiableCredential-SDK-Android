// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.registrars

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.crypto.tink.subtle.Hex
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.*
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.OperationData
import com.microsoft.portableIdentity.sdk.identifier.SuffixData
import com.microsoft.portableIdentity.sdk.identifier.document.*
import com.microsoft.portableIdentity.sdk.resolvers.HttpResolver
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest
import kotlin.experimental.and
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class SidetreeRegistrarInstrumentedTest {

    private val registrar: IRegistrar
    private val resolver: IResolver
    private val logger = ConsoleLogger()
    private val cryptoOperations: CryptoOperations
    private val androidSubtle: SubtleCrypto
    private lateinit var did: String
    private val ecSubtle: EllipticCurveSubtleCrypto

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, logger)
        androidSubtle = AndroidSubtle(keyStore, logger)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle, logger)
//        registrar = SidetreeRegistrar("https://beta.ion.microsoft.com/api/1.0/register", logger)
        registrar = SidetreeRegistrar("http://10.91.6.163:3000", logger)
//        resolver = HttpResolver("https://beta.discover.did.microsoft.com/1.0/identifiers", logger)
        resolver = HttpResolver("http://10.91.6.163:3000", logger)
        cryptoOperations = CryptoOperations(androidSubtle, keyStore, logger)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

//    @Test
/*    fun didRegistrationAndResolutionTest() {
        val microsoftIdentityHubDocument =
            IdentifierDocumentPayload(
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
                        )
                    )
                ),
                services = listOf(
                    IdentityHubService(
                        id = "#hubEndpoint",
                        publicKey = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                        serviceEndpoint = ServiceHubEndpoint(
                            listOf("https://beta.hub.microsoft.com/")
                        )
                    )
                )
            )
        val alias = Base64Url.encode(Random.nextBytes(16), logger)
        val signatureKeyReference = "signature"
        val encryptionKeyReference = "encryption"
        val personaEncKeyRef = "$alias.$encryptionKeyReference"
        val personaSigKeyRef = "$alias.$signatureKeyReference"
        val encKey = cryptoOperations.generateKeyPair(personaEncKeyRef, KeyType.RSA)
        val sigKey = cryptoOperations.generateKeyPair(personaSigKeyRef, KeyType.EllipticCurve)
        val encJwk = encKey.toJWK()
        val sigJwk = sigKey.toJWK()
        // RSA key
        val encPubKey = IdentifierDocumentPublicKey(
            id = encJwk.kid!!,
            type = LinkedDataKeySpecification.RsaSignature2018.values.first(),
            publicKeyJwk = encJwk
        )
        // Secp256k1 key
        val sigPubKey = IdentifierDocumentPublicKey(
            id = sigJwk.kid!!,
            type = LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values.first(),
            publicKeyJwk = sigJwk
        )
        var hubService: IdentifierDocumentService? = null
        val identityHubDid = listOf("did:test:hub.id")
        if (!identityHubDid.isNullOrEmpty()) {
            logger.debug("Adding Microsoft Identity Hub")
            val microsoftHub = Identifier(
                microsoftIdentityHubDocument,
                "",
                "",
                "",
                cryptoOperations,
                logger,
                resolver,
                registrar
            )
            hubService = IdentityHubService.create(
                id = "#hub",
                keyStore = cryptoOperations.keyStore,
                signatureKeyRef = personaEncKeyRef,
                instances = listOf(microsoftHub),
                logger = logger
            )
        }
        val document = RegistrationDocument(
            context = "https://w3id.org/did/v1",
            publicKeys = listOf(encPubKey, sigPubKey),
            services = if (hubService != null) {
                listOf(hubService)
            } else {
                null
            }
        )
        lateinit var registeredIdentifierDocument: IdentifierDocument
        var resolvedIdentifierDocument: IdentifierDocument
        runBlocking {
            registeredIdentifierDocument = registrar.register(document, personaSigKeyRef, cryptoOperations)
            assertThat(registeredIdentifierDocument).isNotNull()
            did = registeredIdentifierDocument.id
        }

        runBlocking {
            val identifier = resolver.resolve(did, cryptoOperations)
            resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isEqualToComparingFieldByFieldRecursively(registeredIdentifierDocument)
        }
    }*/

    private fun hash(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    @Test
    fun getPublicKeyTest() {
        val keyReference = "testkeys#1"
        val keyPair = cryptoOperations.generateKeyPair(keyReference, KeyType.EllipticCurve)
        val pubKeyJWK = keyPair.toJWK()
        val x = Base64.decode(pubKeyJWK.x!!, logger)
        val y = Base64.decode(pubKeyJWK.y!!, logger)
        var yForCompressedHex = "0" + (2 + (y[y.size - 1] and 1)).toString()
        val xForCompressedHex = Hex.encode(x)
        val compressedHexPublicKey = """$yForCompressedHex$xForCompressedHex"""
        println(compressedHexPublicKey + "  and its length is " + compressedHexPublicKey.length)
    }

    @Test
    fun longFormCreationTest() {
        var idDoc: IdentifierDocumentPayload = IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKey(
                    id = "#key1",
                    type = "Secp256k1VerificationKey2018",
                    publicKeyHex = "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
                )
            )
        )
        val idDocPatches = IdentifierDocumentPatch("replace", idDoc)
        val nextUpdateOtp = Base64Url.encode(Random.Default.nextBytes(32), logger)
        val nextUpdateOtpHash = byteArrayOf(18, 32) + hash(stringToByteArray(nextUpdateOtp))
        val nextUpdateOtpHashString = Base64Url.encode(nextUpdateOtpHash, logger)

        val operationData = OperationData(nextUpdateOtpHashString, listOf(idDocPatches))
        val opDataJson = Serializer.stringify(OperationData.serializer(), operationData)
        val opDataByteArray = stringToByteArray(opDataJson)
        val opDataHashed = byteArrayOf(18, 32) + hash(opDataByteArray)
        val opDataHash = Base64Url.encode(opDataHashed, logger)
        val opDataEncodedString = Base64Url.encode(opDataByteArray, logger)

        val nextRecoveryOtp = Base64Url.encode(Random.Default.nextBytes(32), logger)
        val nextRecoveryOtpHash = byteArrayOf(18, 32) + hash(stringToByteArray(nextRecoveryOtp))
        val nextRecoveryOtpHashString = Base64.encode(nextRecoveryOtpHash, logger)

        val suffixData = SuffixData(
            opDataHash,
            RecoveryKey("03f513461b26cfeb508c79ae884f1090e8e431d06bbc6ae52eea31fd381bc52fa5"),
            nextRecoveryOtpHashString
        )
        val suffixDataJson = Serializer.stringify(SuffixData.serializer(), suffixData)
        val suffixDataEncodedString = Base64Url.encode(stringToByteArray(suffixDataJson), logger)
        val suffixDataHashed = byteArrayOf(18, 32) + hash(stringToByteArray(suffixDataJson))
        val uniqueSuffix = Base64Url.encode(suffixDataHashed, logger)
        val did = "did:ion:test:$uniqueSuffix"

        val regDoc = RegistrationDocument("create", suffixDataEncodedString, opDataEncodedString)
        val regDocJson = Serializer.stringify(RegistrationDocument.serializer(), regDoc)
        val regDocEncodedString = Base64Url.encode(stringToByteArray(regDocJson), logger)

        println("did is $did and initial-state is $regDocEncodedString")
        runBlocking {
            val identifier = resolver.resolve(did, regDocEncodedString, cryptoOperations)
            val resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isNotNull
        }
    }

    @Test
    fun idCreationTest() {
        val alias = Base64Url.encode(Random.nextBytes(16), logger)
        runBlocking {
            val id = Identifier.createLongFormIdentifier(alias, cryptoOperations, logger, "", "", resolver, registrar)
            assertThat(id).isNotNull
        }
    }
}