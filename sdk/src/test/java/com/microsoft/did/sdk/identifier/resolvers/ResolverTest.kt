// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.resolvers

import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.did.sdk.util.controlflow.NotFoundException
import com.microsoft.did.sdk.util.controlflow.ResolverException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResolverTest {
    private val identifierRepository: IdentifierRepository = mockk()
    private val expectedIdentifierDocumentString =
        """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"}],"verificationMethod":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"authentication":["#Va8_sign_ION_1"]},"methodMetadata":{"operationPublicKeys":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc","y":"BCpmR6pekJvBqEbpH4LpnuLwSJLGdrNPr3AhWIGmbX4"}},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-05-31T09:20:41.162Z","duration":"118.4098ms"}}"""
    private val expectedIdentifierResponse =
        defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
    private val expectedIdentifier = expectedIdentifierResponse.didDocument.id
    private val invalidIdentifier = "invalid-did"

    @Test
    fun successfulResolutionTest() {
        val resolver = Resolver("", identifierRepository)
        coEvery { identifierRepository.resolveIdentifier("", expectedIdentifier) } returns Result.Success(expectedIdentifierResponse)
        runBlocking {
            val actualIdentifierDocument = resolver.resolve(expectedIdentifier)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifierDocument as Result.Success).payload.id).isEqualTo(expectedIdentifier)
        }
    }

    @Test
    fun failedResolutionInvalidIdTest() {
        val resolver = Resolver("", identifierRepository)
        coEvery { identifierRepository.resolveIdentifier("", invalidIdentifier) } returns Result.Failure(
            NotFoundException(
                "Not Found",
                true,
            )
        )
        runBlocking {
            val actualResult = resolver.resolve(invalidIdentifier)
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertThat((actualResult as Result.Failure).payload).isInstanceOf(ResolverException::class.java)
            assertThat(actualResult.payload.cause).isInstanceOf(NotFoundException::class.java)
        }
    }

    @Test
    fun failedResolutionNetworkConnectionTest() {
        val resolver = Resolver("invalidUrl", identifierRepository)
        coEvery {
            identifierRepository.resolveIdentifier(
                "invalidUrl",
                expectedIdentifier
            )
        } returns Result.Failure(LocalNetworkException("Failed to send request."))
        runBlocking {
            val actualResult = resolver.resolve(expectedIdentifier)
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertThat((actualResult as Result.Failure).payload).isInstanceOf(ResolverException::class.java)
            assertThat(actualResult.payload.cause).isInstanceOf(LocalNetworkException::class.java)
        }
    }
}