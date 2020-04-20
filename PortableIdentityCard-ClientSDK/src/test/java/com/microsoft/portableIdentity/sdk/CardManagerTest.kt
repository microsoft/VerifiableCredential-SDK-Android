package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardManagerTest {

    @Mock
    lateinit var mockedValidator: OidcRequestValidator

    @Mock
    lateinit var mockedFormatter: OidcResponseFormatter

    @Mock
    lateinit var mockedCardRepository: CardRepository

    @Mock
    lateinit var mockedContract: PicContract

    @Mock
    lateinit var mockedRequest: OidcRequest

    lateinit var cardManager: CardManager


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        cardManager = CardManager(mockedCardRepository, mockedValidator, mockedFormatter)
    }

    @Test
    fun `get Issuance Request`() {
        runBlocking {
            val contractUrl = "http://testcontract.com"
            `when`<Result<PicContract>>(mockedCardRepository.getContract(contractUrl)).thenReturn(Result.Success(mockedContract))
            val results = cardManager.getIssuanceRequest(contractUrl)
            assertEquals(mockedContract, (results as Result.Success).payload.contract)
        }
    }

    @Test
    fun `Validate Valid Request`() {
        runBlocking {
            `when`<Result<Boolean>>(mockedValidator.validate(mockedRequest)).thenReturn(Result.Success(true))
            val results = cardManager.isValid(mockedRequest)
            assertTrue((results as Result.Success).payload)
        }
    }

    @Test
    fun `Validate Invalid Request`() {
        runBlocking {
            `when`<Result<Boolean>>(mockedValidator.validate(mockedRequest)).thenReturn(Result.Success(false))
            val resultsFalse = cardManager.isValid(mockedRequest)
            assertFalse((resultsFalse as Result.Success).payload)
        }
    }
}