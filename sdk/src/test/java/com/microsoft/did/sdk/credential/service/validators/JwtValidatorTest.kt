package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ValidatorException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.security.PublicKey
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class JwtValidatorTest {

    private val mockedIdentifierDocument: IdentifierDocument = mockk()

    private val mockedIdentifierDocumentPublicKey: IdentifierDocumentPublicKey = mockk()

    private val mockedPublicKey: PublicKey = mockk()

    private val mockedJwsToken: JwsToken = mockk()

    private val mockedResolver: Resolver = mockk()

    private val validator: JwtValidator

    private val expectedDid: String = "did:test:4235"
    private val expectedKid: String = "$expectedDid#kidTest2353"


    init {
        validator = JwtValidator(mockedResolver)
        setUpResolver()
        mockkObject(JwsToken)
    }

    private fun setUpResolver() {
        every { mockedIdentifierDocument.verificationMethod } returns listOf(mockedIdentifierDocumentPublicKey)
        every { mockedIdentifierDocumentPublicKey.toPublicKey() } returns mockedPublicKey
    }

    @Test
    fun `valid signature is validated successfully`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.Success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKey)) } returns true
        every { mockedJwsToken.getKeyId() } returns expectedKid
        runBlocking {
            val actualValidationResult = validator.verifySignature(mockedJwsToken)
            assertTrue(actualValidationResult)
        }
    }

    @Test
    fun `invalid signature fails successfully`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.Success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKey)) } returns false
        every { mockedJwsToken.getKeyId() } returns expectedKid
        runBlocking {
            val actualValidationResult = validator.verifySignature(mockedJwsToken)
            assertFalse(actualValidationResult)
        }
    }

    @Test
    fun `throws when no key id specified`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.Success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKey)) } returns true
        every { mockedJwsToken.getKeyId() } returns null
        runBlocking {
            try {
                validator.verifySignature(mockedJwsToken)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ValidatorException::class.java)
            }
        }
    }

    @Test
    fun `throws when unable to resolve identifier document`() {
        val expectedException = ValidatorException("test")
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.Failure(expectedException)
        every { mockedJwsToken.verify(listOf(mockedPublicKey)) } returns true
        every { mockedJwsToken.getKeyId() } returns expectedKid
        runBlocking {
            try {
                validator.verifySignature(mockedJwsToken)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ValidatorException::class.java)
            }
        }
    }
}