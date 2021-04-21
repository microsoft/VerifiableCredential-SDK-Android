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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class MicrosoftUnprotectedBackup2020Test {
    private val walletMetadata = WalletMetadata()

    @Serializable
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
    fun vcsToIteratorTest() {
        val iterator = backup.vcsToIterator( Json { SerializersModule {
            polymorphic(VCMetadata::class) {
                subclass(TestVCMetadata::class)
            }
        } } )
    }
}