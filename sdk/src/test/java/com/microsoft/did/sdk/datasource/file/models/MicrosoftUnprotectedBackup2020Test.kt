// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class MicrosoftUnprotectedBackup2020Test {
    private val walletMetadata = WalletMetadata()

    @SerialName("TestVCMetadata")
    private class TestVCMetadata(override val displayContract: DisplayContract) : VCMetadata() {}
    private val vcMetadata = TestVCMetadata(
        VerifiableCredentialUtil.testDisplayContract
    )
    private val rawId = VerifiableCredentialUtil.rawIdentifier

    private val backup = MicrosoftUnprotectedBackup2020(
        mapOf("test" to VerifiableCredentialUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(rawId)
    )

    @Test
    fun typeShouldMatchStaticTest() {
        assertEquals(MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE, backup.type, "types should match")
    }

    @Test
    fun importHappyPathTest() {
        runBlocking {
            val testMetaImportCallback: suspend (WalletMetadata) -> Unit = {
                assertEquals(walletMetadata, it, "Wallet metadata does not match")
            }
            val testVCImportCallback: suspend (VerifiableCredential, VCMetadata) -> Unit = { vc, meta ->
                assertEquals(VerifiableCredentialUtil.testVerifiedCredential, vc, "VC doesn't match")
                assertEquals(VerifiableCredentialUtil.testDisplayContract, meta.displayContract, "Display contract doesn't match")
            }
            val testVCListCallback: suspend() -> List<String> = {
                emptyList()
            }
            val testDeleteCallback: suspend(String) -> Unit = {
                fail("should not be called")
            }
            val idRepo: IdentifierRepository = mockk()
            coEvery { idRepo.queryAllLocal() } returns ( emptyList() )
            coEvery { idRepo.insert(VerifiableCredentialUtil.testIdentifer) } returns(Unit)
            val keyStore: EncryptedKeyStore = mockk()
            every { keyStore.containsKey(any()) } returns(false)
            every { keyStore.storeKey(VerifiableCredentialUtil.signKey, "sign") } returns(Unit)
            every { keyStore.storeKey(VerifiableCredentialUtil.updateKey, "update") } returns(Unit)
            every { keyStore.storeKey(VerifiableCredentialUtil.recoverKey, "recover") } returns(Unit)
            every { keyStore.storeKey(VerifiableCredentialUtil.encryptKey, "encrypt") } returns(Unit)
            backup.import(testMetaImportCallback, testVCImportCallback, testVCListCallback, testDeleteCallback, idRepo, keyStore, Json.Default)
        }
    }
}