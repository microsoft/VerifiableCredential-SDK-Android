// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.identifierOperations.ResolveIdentifierNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ServiceErrorException
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifierRepositoryTest {
    private var identifierRepository: IdentifierRepository
    private val identifierDao: IdentifierDao = mockk()
    private val expectedIdentifierDocumentString =
        """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"}],"publicKey":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"authentication":["#Va8_sign_ION_1"]},"methodMetadata":{"operationPublicKeys":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc","y":"BCpmR6pekJvBqEbpH4LpnuLwSJLGdrNPr3AhWIGmbX4"}},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-05-31T09:20:41.162Z","duration":"118.4098ms"}}"""
    private val expectedIdentifierDocument =
        defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
    private val expectedIdentifier =
        "did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"

    init {
        val sdkDatabase: SdkDatabase = mockk()
        val apiProvider: ApiProvider = mockk()
        every { sdkDatabase.identifierDao() } returns identifierDao
        identifierRepository = IdentifierRepository(sdkDatabase, apiProvider)
    }

    @Test
    fun insertAndRetrieveIdentifierByIdTest() {
        val expectedIdentifier = Identifier(
            "did:ion:test:testId1",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName1"
        )
        coJustRun { identifierDao.insert(expectedIdentifier) }
        coEvery { identifierDao.queryByIdentifier(expectedIdentifier.id) } returns expectedIdentifier
        runBlocking {
            identifierRepository.insert(expectedIdentifier)
            val actualIdentifier = identifierRepository.queryByIdentifier(expectedIdentifier.id)
            assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(expectedIdentifier)
        }
    }

    @Test
    fun insertAndRetrieveIdentifierByNameTest() {
        val expectedIdentifier = Identifier(
            "did:ion:test:testId2",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName2"
        )
        coJustRun { identifierDao.insert(expectedIdentifier) }
        coEvery { identifierDao.queryByName("testIdentifierName2") } returns expectedIdentifier
        runBlocking {
            identifierRepository.insert(expectedIdentifier)
            val actualIdentifier = identifierRepository.queryByName("testIdentifierName2")
            assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(expectedIdentifier)
        }
    }

    @Test
    fun resolveIdentifierTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Success(expectedIdentifierDocument)
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", suppliedIdentifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifierDocument as Result.Success).payload.didDocument.id).isEqualTo(expectedIdentifier)
        }
        coVerify(exactly = 1) {
            anyConstructed<ResolveIdentifierNetworkOperation>().fire()
        }
    }

    @Test
    fun resolveInvalidIdentifierTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Failure(
            ServiceErrorException(
                "123",
                "Not found",
                true
            )
        )
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", suppliedIdentifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Failure::class.java)
            assertThat((actualIdentifierDocument as Result.Failure).payload).isInstanceOf(ServiceErrorException::class.java)
        }
        coVerify(exactly = 1) {
            anyConstructed<ResolveIdentifierNetworkOperation>().fire()
        }
    }
}
