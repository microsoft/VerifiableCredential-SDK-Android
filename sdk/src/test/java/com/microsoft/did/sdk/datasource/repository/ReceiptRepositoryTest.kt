// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.ReceiptDao
import com.microsoft.did.sdk.getOrAwaitValue
import com.microsoft.did.sdk.identifier.models.Identifier
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class ReceiptRepositoryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var vcContract: VerifiableCredentialContract = mockk()
    private val issuanceRequest: IssuanceRequest
    private var issuanceResponse: IssuanceResponse
    private val receiptRepository: ReceiptRepository
    private val mockedDatabase: SdkDatabase = mockk()
    private val mockedReceiptDao: ReceiptDao = mockk()
    private val mockedPairwiseIdentifier: Identifier = mockk()

    private val expectedCredentialType1 = "type2354"
    private val expectedCredentialType2 = "type4009"

    private val attestations: CredentialAttestations = mockk()
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"

    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        issuanceRequest = IssuanceRequest(vcContract, "testContractUrl")
        every { issuanceRequest.contract.input.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(issuanceRequest, mockedPairwiseIdentifier)
        every { mockedDatabase.receiptDao() } returns mockedReceiptDao
        receiptRepository = ReceiptRepository(mockedDatabase)
    }

    @Test
    fun `test to verify multiple receipt creation for revoke successfully`() {
        val suppliedVCId = "vc1"
        val suppliedEntityName1 = "RP1"
        val suppliedEntityDid1 = "RPDID1"
        val currentTimeForReceipt1 = System.currentTimeMillis()
        val receipt1 = Receipt(ReceiptAction.Revocation, suppliedEntityDid1, suppliedEntityName1, suppliedVCId)
        assertThat(receipt1.vcId).isEqualTo(suppliedVCId)
        assertThat(receipt1.entityName).isEqualTo(suppliedEntityName1)
        assertThat(receipt1.entityIdentifier).isEqualTo(suppliedEntityDid1)
        assertThat(receipt1.activityDate).isGreaterThanOrEqualTo(currentTimeForReceipt1)

        val suppliedEntityName2 = "RP2"
        val suppliedEntityDid2 = "RPDID2"
        val currentTimeForReceipt2 = System.currentTimeMillis()
        val receipt2 = Receipt(ReceiptAction.Revocation, suppliedEntityDid2, suppliedEntityName2, suppliedVCId)
        assertThat(receipt2.vcId).isEqualTo(suppliedVCId)
        assertThat(receipt2.entityName).isEqualTo(suppliedEntityName2)
        assertThat(receipt2.entityIdentifier).isEqualTo(suppliedEntityDid2)
        assertThat(receipt2.activityDate).isGreaterThanOrEqualTo(currentTimeForReceipt2)
        assertThat(receipt1.activityDate).isLessThanOrEqualTo(receipt2.activityDate)
    }

    @Test
    fun `test create receipt by adding empty card id`() {
        val vch: VerifiableCredentialHolder = mockk()
        val presentationAttestation: PresentationAttestation = mockk()
        every { presentationAttestation.credentialType } returns expectedCredentialType1
        val receiptCreationStartTime = System.currentTimeMillis()
        issuanceResponse.requestedVchMap[presentationAttestation] = vch
        val cardId = ""
        every { vch.cardId } returns cardId
        coJustRun { mockedReceiptDao.insert(any()) }
        val expectedReceipt: LiveData<List<Receipt>> = MutableLiveData(
            listOf(Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, cardId))
        )
        every { mockedReceiptDao.getAllReceiptsByVcId(cardId) } returns expectedReceipt
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
            val receipts = receiptRepository.getAllReceiptsByVcId(cardId).getOrAwaitValue()
            assertThat(receipts).isNotNull
            if (receipts != null) {
                val expectedReceiptCount = 1
                assertThat(receipts.size).isEqualTo(expectedReceiptCount)
                val receipt = receipts.first()
                assertThat(receipt.vcId).isEqualTo("")
                assertThat(receipt.entityName).isEqualTo(testEntityName)
                assertThat(receipt.entityIdentifier).isEqualTo(testEntityDid)
                assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
                assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
            }
        }
    }

    @Test
    fun `test create receipt by adding 1 card`() {
        val vch: VerifiableCredentialHolder = mockk()
        val presentationAttestation: PresentationAttestation = mockk()
        every { presentationAttestation.credentialType } returns expectedCredentialType1
        val receiptCreationStartTime = System.currentTimeMillis()
        issuanceResponse.requestedVchMap[presentationAttestation] = vch
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        coJustRun { mockedReceiptDao.insert(any()) }
        val expectedReceipt: LiveData<List<Receipt>> = MutableLiveData(
            listOf(Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, cardId))
        )
        every { mockedReceiptDao.getAllReceiptsByVcId(cardId) } returns expectedReceipt
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
            val receipts = receiptRepository.getAllReceiptsByVcId(cardId).getOrAwaitValue()
            assertThat(receipts).isNotNull
            if (receipts != null) {
                val expectedReceiptCount = 1
                assertThat(receipts.size).isEqualTo(expectedReceiptCount)
                val receipt = receipts.first()
                assertThat(receipt.vcId).isEqualTo(cardId)
                assertThat(receipt.entityName).isEqualTo(testEntityName)
                assertThat(receipt.entityIdentifier).isEqualTo(testEntityDid)
                assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
                assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
            }
        }
    }

    @Test
    fun `test create receipt without adding card`() {
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                issuanceResponse.request.entityIdentifier,
                issuanceResponse.request.entityName,
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
        }
        coVerify(exactly = 0) {
            receiptRepository.insert(any())
        }
    }

    @Test
    fun `test create receipt by adding multiple cards with same type`() {
        val vch1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedCredentialType1
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation1] = vch1
        val vchId1 = "vchId1"
        every { vch1.cardId } returns vchId1
        val vch2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation2.credentialType } returns expectedCredentialType1
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation2] = vch2
        val vchId2 = "vchId2"
        every { vch2.cardId } returns vchId2
        coJustRun { mockedReceiptDao.insert(any()) }
        val receiptCreationStartTime = System.currentTimeMillis()
        val expectedReceipts: LiveData<List<Receipt>> = MutableLiveData(
            listOf(
                Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, vchId1),
                Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, vchId2)
            )
        )
        every { mockedReceiptDao.getAllReceipts() } returns expectedReceipts
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
            val receipts = receiptRepository.getAllReceipts().getOrAwaitValue()
            assertThat(receipts).isNotNull
            if (receipts != null) {
                val expectedReceiptCount = 2
                assertThat(receipts.size).isEqualTo(expectedReceiptCount)
                val receipt = receipts.first()
                assertThat(receipt.vcId).isEqualTo(vchId1)
                assertThat(receipt.entityName).isEqualTo(testEntityName)
                assertThat(receipt.entityIdentifier).isEqualTo(testEntityDid)
                assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
                assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
            }
        }
    }

    @Test
    fun `test create receipt by adding multiple cards with different types`() {
        val vch1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedCredentialType1
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation1] = vch1
        val cardId1 = "testCardId1"
        every { vch1.cardId } returns cardId1
        val vch2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation2.credentialType } returns expectedCredentialType2
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation2] = vch2
        val cardId2 = "testCardId2"
        every { vch2.cardId } returns cardId2
        coJustRun { mockedReceiptDao.insert(any()) }
        val receiptCreationStartTime = System.currentTimeMillis()
        val expectedReceipts: LiveData<List<Receipt>> = MutableLiveData(
            listOf(
                Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, cardId1),
                Receipt(ReceiptAction.Presentation, testEntityDid, testEntityName, cardId2)
            )
        )
        every { mockedReceiptDao.getAllReceipts() } returns expectedReceipts
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
            val receipts = receiptRepository.getAllReceipts().getOrAwaitValue()
            assertThat(receipts).isNotNull
            if (receipts != null) {
                val expectedReceiptCount = 2
                assertThat(receipts.size).isEqualTo(expectedReceiptCount)
                val receipt1 = receipts.first()
                assertThat(receipt1.vcId).isEqualTo(cardId1)
                assertThat(receipt1.entityName).isEqualTo(testEntityName)
                assertThat(receipt1.entityIdentifier).isEqualTo(testEntityDid)
                assertThat(receipt1.action).isEqualTo(ReceiptAction.Presentation)
                val receipt2 = receipts.last()
                assertThat(receipt2.vcId).isEqualTo(cardId2)
                assertThat(receipt2.entityName).isEqualTo(testEntityName)
                assertThat(receipt2.entityIdentifier).isEqualTo(testEntityDid)
                assertThat(receipt2.action).isEqualTo(ReceiptAction.Presentation)
                assertThat(receipt1.activityDate).isLessThanOrEqualTo(receipt2.activityDate)
                assertThat(receipt1.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
            }
        }
    }

    @Test
    fun `test create receipt by adding empty entity information`() {
        val vch: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedCredentialType1
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation1] = vch
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        coJustRun { mockedReceiptDao.insert(any()) }
        val receiptCreationStartTime = System.currentTimeMillis()
        val expectedReceipt: LiveData<List<Receipt>> = MutableLiveData(
            listOf(Receipt(ReceiptAction.Presentation, "", "", cardId))
        )
        every { mockedReceiptDao.getAllReceiptsByVcId(cardId) } returns expectedReceipt
        runBlocking {
            receiptRepository.createAndSaveReceiptsForVCs(
                "",
                "",
                ReceiptAction.Presentation,
                issuanceResponse.requestedVchMap.values.map { it.cardId }
            )
            val receipts = receiptRepository.getAllReceiptsByVcId(cardId).getOrAwaitValue()
            assertThat(receipts).isNotNull
            if (receipts != null) {
                val expectedReceiptCount = 1
                assertThat(receipts.size).isEqualTo(expectedReceiptCount)
                val receipt = receipts.first()
                assertThat(receipt.vcId).isEqualTo(cardId)
                assertThat(receipt.entityName).isEqualTo("")
                assertThat(receipt.entityIdentifier).isEqualTo("")
                assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
                assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
            }
        }
    }
}
