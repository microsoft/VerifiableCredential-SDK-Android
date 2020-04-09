package com.microsoft.portableIdentity.sdk.registrars

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.IdentifierDocumentService
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.portableIdentity.sdk.identifier.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.resolvers.HttpResolver
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class SidetreeRegistrarInstrumentedTest {

    private val registrar: IRegistrar
    private val resolver: IResolver
    private val cryptoOperations: CryptoOperations
    private val androidSubtle: SubtleCrypto
    private lateinit var did: String

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context)
        androidSubtle = AndroidSubtle(keyStore)
        val ecSubtle = EllipticCurveSubtleCrypto(androidSubtle)
        registrar = SidetreeRegistrar("https://beta.ion.microsoft.com/api/1.0/register")
        resolver = HttpResolver("https://beta.discover.did.microsoft.com/1.0/identifiers")
        cryptoOperations = CryptoOperations(androidSubtle, keyStore)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
        cryptoOperations.subtleCryptoFactory.addMessageDigest(
            name = Sha.Sha256.name,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

    @Test
    fun didRegistrationAndResolutionTest() {
        val microsoftIdentityHubDocument = IdentifierDocument(
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
                    serviceEndpoint = ServiceHubEndpoint(listOf("https://beta.hub.microsoft.com/"))
                )
            )
        )
        val alias = Base64Url.encode(Random.nextBytes(16))
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
            val microsoftHub = Identifier(microsoftIdentityHubDocument, "", "", "", cryptoOperations, resolver, registrar)
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
            println(did)
        }

        runBlocking {
            val identifier = resolver.resolve(did, cryptoOperations)
            resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isEqualToComparingFieldByFieldRecursively(registeredIdentifierDocument)
        }
    }

    @Test
    fun resolutionTest() {
        did = "did:ion:test:EiBvuqdsYCLWgkDlY_Jc9Hnk4SqDxqq6VTmFfygRII49FQ"
        runBlocking {
            val identifier = resolver.resolve(did, cryptoOperations)
            val resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isNotNull()
        }
    }
}