// Copyright (c) Microsoft Corporation. All rights reserved

package android.util

import com.microsoft.did.sdk.backup.content.microsoft2020.RawIdentity
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.util.Base64URL
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// cryptographically correct and consistent Verifiable Credential data
object BackupTestUtil {
    private const val testDid = "did:web:localhost"
    val signKey: ECKey = ECKeyGenerator(Curve.P_256).keyID("sign").keyUse(KeyUse.SIGNATURE).generate()
    val updateKey: ECKey = ECKeyGenerator(Curve.P_256).keyID("update").keyUse(KeyUse.SIGNATURE).generate()
    val recoverKey: ECKey = ECKeyGenerator(Curve.P_256).keyID("recover").keyUse(KeyUse.SIGNATURE).generate()
    val encryptKey: RSAKey = RSAKeyGenerator(4096).keyID("encrypt").keyUse(KeyUse.ENCRYPTION).generate()
    val testDisplayContract = DisplayContract(
        locale = "en-US",
        contract = "http://localhost/contract",
        card = CardDescriptor("Test", "n/a", "#f64ded", "#a80aa5", Logo(description = "test"), "test card"),
        consent = ConsentDescriptor("You shouldn't approve this", "Reject this card"),
        claims = emptyMap()
    )
    private const val jti = "iri:vc:test"
    private val testVerifiableCredentialContent = VerifiableCredentialContent(
        jti,
        VerifiableCredentialDescriptor(
            listOf(),
            listOf(),
            mapOf()
        ),
        testDid,
        testDid,
        0,
        0,
        "INVALID: FOR TESTING USE ONLY"
    )
    val testVerifiedCredential: VerifiableCredential by lazy {
        val jws = JwsToken(
            JWSObject(
                JWSHeader(JWSAlgorithm.ES256),
                Payload(Base64URL.encode(Json.Default.encodeToString(testVerifiableCredentialContent)))
            )
        )
        jws.sign(
            signKey, JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(signKey.keyID)
                .build()
        )
        VerifiableCredential(
            jti,
            jws.serialize(),
            testVerifiableCredentialContent
        )
    }
    val testIdentifer = Identifier(
        testDid,
        "sign",
        "encrypt",
        "recover",
        "update",
        "testIdentifier"
    )
    val rawIdentifier = RawIdentity(
        testDid,
        "testIdentifier",
        listOf(signKey, encryptKey, recoverKey, updateKey),
        "recover",
        "update"
    )

    fun getMockKeyStore(): EncryptedKeyStore {
        val keyStore = mockk<EncryptedKeyStore>();
        every { keyStore.getKey(recoverKey.keyID) } returns (recoverKey)
        every { keyStore.containsKey(recoverKey.keyID) } returns true
        every { keyStore.getKey(updateKey.keyID) } returns (updateKey)
        every { keyStore.containsKey(updateKey.keyID) } returns true
        every { keyStore.getKey(signKey.keyID) } returns (signKey)
        every { keyStore.containsKey(signKey.keyID) } returns true
        every { keyStore.getKey(encryptKey.keyID) } returns (encryptKey)
        every { keyStore.containsKey(encryptKey.keyID) } returns true
        every { keyStore.storeKey(any(), any()) } returns Unit
        every { keyStore.getKey(any()) } returns (recoverKey)
        return keyStore
    }

    fun getMockIdentifierRepository(): IdentifierRepository {
        val identifierRepository = mockk<IdentifierRepository>()
        coEvery { identifierRepository.queryByIdentifier(testDid) } returns (testIdentifer)
        coEvery { identifierRepository.queryAllLocal() } returns (listOf(testIdentifer))
        coEvery { identifierRepository.queryByName(testIdentifer.name) } returns (testIdentifer)
        coEvery { identifierRepository.insert(any()) } returns Unit
        coEvery { identifierRepository.deleteAll() } returns Unit
        return identifierRepository
    }
}