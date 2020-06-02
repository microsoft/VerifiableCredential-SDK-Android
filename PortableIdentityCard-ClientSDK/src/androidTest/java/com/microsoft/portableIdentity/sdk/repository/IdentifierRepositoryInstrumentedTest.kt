// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.PortableIdentitySdk
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.portableIdentity.sdk.repository.dao.IdentifierDao
import com.microsoft.portableIdentity.sdk.repository.networking.identifierOperations.ResolveIdentifierNetworkOperation
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ServiceErrorException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifierRepositoryInstrumentedTest {
    private var identifierRepository: IdentifierRepository
    private val expectedIdentifierDocumentString =
        """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"}],"publicKey":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"authentication":["#Va8_sign_ION_1"]},"methodMetadata":{"operationPublicKeys":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc","y":"BCpmR6pekJvBqEbpH4LpnuLwSJLGdrNPr3AhWIGmbX4"}},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-05-31T09:20:41.162Z","duration":"118.4098ms"}}"""
    private val expectedIdentifierDocument = Serializer().parse(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
    private val expectedIdentifier =
        "did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PortableIdentitySdk.init(context)
        identifierRepository = spyk(PortableIdentitySdk.identifierManager.identifierRepository)
    }

    @Test
    fun insertAndRetrieveIdentifierTest() {
        val identifier = Identifier(
            "did:ion:test:testId1",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName1"
        )
        val identifierDao: IdentifierDao = mockk()
        justRun { identifierDao.insert(identifier) }
        identifierRepository.insert(identifier)
        every { identifierDao.queryByIdentifier(identifier.id) } returns identifier
        val actualIdentifier = identifierRepository.queryByIdentifier(identifier.id)
        assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(identifier)
    }

    @Test
    fun insertAndRetrieveIdentifierByNameTest() {
        val identifier = Identifier(
            "did:ion:test:testId2",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName2"
        )
        val identifierDao: IdentifierDao = mockk()
        justRun { identifierDao.insert(identifier) }
        identifierRepository.insert(identifier)
        every { identifierDao.queryByName("testIdentifierName2") } returns identifier
        val actualIdentifier = identifierRepository.queryByName("testIdentifierName2")
        assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(identifier)
    }

    @Test
    fun resolveIdentifierTest() {
        val identifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Success(expectedIdentifierDocument)
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", identifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifierDocument as Result.Success).payload.didDocument.id).isEqualTo(expectedIdentifier)
        }
    }

    @Test
    fun resolveInvalidIdentifierTest() {
        val identifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Failure(ServiceErrorException("Not found"))
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", identifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Failure::class.java)
            assertThat((actualIdentifierDocument as Result.Failure).payload).isInstanceOf(ServiceErrorException::class.java)
        }
    }
}
