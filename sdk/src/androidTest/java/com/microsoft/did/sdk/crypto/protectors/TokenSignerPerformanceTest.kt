/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.TestKeys
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.identifier.SidetreePayloadProcessor
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.decodeFromString
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.lang.Exception

@RunWith(AndroidJUnit4ClassRunner::class)
class TokenSignerPerformanceTest {
    private val testName = "TokenSignerPerformanceTest"
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val cryptoKeyPair: CryptoKeyPair
    private val keyReference = "KeyReference1"
    private val serializer = Json
    private val signer: TokenSigner
    private val validator: JwtValidator
    private val cryptoOperations: CryptoOperations
    private val identifierCreator: IdentifierCreator
    private val identifierRepository: IdentifierRepository
    private val identifier: Identifier
    private val payload = "the Answer to the Ultimate Question of Life, the Universe, and Everything"

    init {
        println("PerfTest->(${getTestName()}) - Start init")
        val startTime = getStartTime()
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, defaultTestSerializer)
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )

        val publicKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.publicKey)
        cryptoOperations = CryptoOperations(ellipticCurveSubtleCrypto, keyStore, EllipticCurvePairwiseKey())
        val sidetreePayloadProcessor = SidetreePayloadProcessor(defaultTestSerializer)
        val identifierDao: IdentifierDao = mockk()
        val sdkDatabase: SdkDatabase = mockk()
        val apiProvider: ApiProvider = mockk()
        every { sdkDatabase.identifierDao() } returns identifierDao
        identifierRepository = IdentifierRepository(sdkDatabase, apiProvider)
        identifierCreator = IdentifierCreator(cryptoOperations, sidetreePayloadProcessor)
        val result: Result<Identifier> = identifierCreator.create("ION")
        if (result !is Result.Success) {
            throw Exception("Could not create identifier")
        }
        identifier = result.payload
        val resolver = setUpResolver(identifier, publicKey)

        signer = TokenSigner(cryptoOperations, serializer)
        validator = JwtValidator(cryptoOperations, resolver, defaultTestSerializer)

        println("PerfTest->(${getTestName()}) - End init: ${timer(startTime)}")
    }

    fun getTestName(): String {
        return this.testName
    }

    fun getStartTime(): Long {
        return System.nanoTime()
    }

    fun timer(start: Long): String {
        val timing = System.nanoTime() - start
        return (timing / 1000).toString()
    }

    private fun setUpResolver(identifier: Identifier, publicKey: JsonWebKey): Resolver {
        println("PerfTest->(${getTestName()}) - id: ${identifier.id}")
        println("PerfTest->(${getTestName()}) - kid: ${identifier.signatureKeyReference}")
        val resolver = Resolver("https://beta.discover.did.microsoft.com/1.0/identifiers", identifierRepository)
        /*
        val expectedIdentifierDocumentString =
            """{"@context":"https://www.w3.org/ns/did-resolution/v1",
                |"didDocument":{"id": "${identifier.id}" ,
                |"@context":["https://www.w3.org/ns/did/v1", {"@base":"${identifier.id}"}],
                |"publicKey":[{"id":"#${identifier.signatureKeyReference}","controller":"","type":"EcdsaSecp256k1VerificationKey2019",
                |"publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],
                |"authentication":["#${identifier.signatureKeyReference}"]},"methodMetadata":{"operationPublicKeys":[{"id":"#${identifier.signatureKeyReference}","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc","y":"BCpmR6pekJvBqEbpH4LpnuLwSJLGdrNPr3AhWIGmbX4"}},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-05-31T09:20:41.162Z","duration":"118.4098ms"}}""".trimMargin()
        val expectedIdentifierResponse =
            defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
        */
        val key = listOf(
            IdentifierDocumentPublicKey(
                id = identifier.signatureKeyReference,
                type = "EcdsaSecp256k1VerificationKey2019",
                publicKeyJwk = publicKey
            )
        )
        //coEvery { identifierRepository.resolveIdentifier("https://beta.discover.did.microsoft.com/1.0/identifiers", identifier.id) } returns Result.Success(expectedIdentifierResponse)
        coEvery { resolver.resolve(identifier.id) } returns Result.Success(IdentifierDocument(key, identifier.id))

        return resolver
    }

    @Test
    fun signAndVerifySignaturePerformanceTest() {
        for (loop in 0..9) {
            println("PerfTest->(${getTestName()}) in  μs - ${loop}: Start sign: 0")
            var startTime = getStartTime()
            // use result.payload (Identifier)
            val signedPayload = signer.signWithIdentifier(payload, identifier)
            println("PerfTest->(${getTestName()}) in  μs - ${loop}: End sign: ${timer(startTime)}")

            println("PerfTest->(${getTestName()}) in  μs - ${loop}: Start deserialize: 0")
            startTime = getStartTime()
            JwsToken.deserialize(signedPayload, defaultTestSerializer)
            println("PerfTest->(${getTestName()}) in  μs - ${loop}: End deserialize: ${timer(startTime)}")

            runBlocking {
                println("PerfTest->(${getTestName()}) in  μs - ${loop}: Start verify: 0")
                startTime = getStartTime()
                //val response = identifierRepository.resolveIdentifier("https://beta.discover.did.microsoft.com/1.0/identifiers", identifier.id)

                //val actualValidationResult = validator.verifySignature(signature)
                println("PerfTest->(${getTestName()}) in  μs - ${loop}: End verify: ${timer(startTime)}")
            }
        }
    }
}