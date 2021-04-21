// Copyright (c) Microsoft Corporation. All rights reserved

package android.util

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.util.Base64URL
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// cryptographically correct and consistent Verifiable Credential data
object VerifiableCredentialUtil {
    val testDid = "did:web:localhost"
    val signKey = ECKeyGenerator(Curve.P_256).keyID("sign").keyUse(KeyUse.SIGNATURE).generate()
    val updateKey = ECKeyGenerator(Curve.P_256).keyID("update").keyUse(KeyUse.SIGNATURE).generate()
    val recoverKey = ECKeyGenerator(Curve.P_256).keyID("recover").keyUse(KeyUse.SIGNATURE).generate()
    val encryptKey = RSAKeyGenerator(4096).keyID("encrypt").keyUse(KeyUse.ENCRYPTION).generate()
    val testDisplayContract = DisplayContract(
        locale = "en-US",
        contract = "http://localhost/contract",
        card = CardDescriptor("Test", "n/a", "#f64ded", "#a80aa5", Logo(description = "test"), "test card"),
        consent = ConsentDescriptor("You shouldn't approve this", "Reject this card"),
        claims = emptyMap()
    )
    private const val jti = "iri:vc:test"
    val testVerifiableCredentialContent = VerifiableCredentialContent(
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
        val jws = JwsToken(JWSObject(JWSHeader(JWSAlgorithm.ES256), Payload(Base64URL.encode(Json.Default.encodeToString(testVerifiableCredentialContent)))))
        jws.sign(signKey)
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
}