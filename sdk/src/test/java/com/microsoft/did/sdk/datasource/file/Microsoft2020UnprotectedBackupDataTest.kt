// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.datasource.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.datasource.backup.content.microsoft2020.VcMetadata
import com.microsoft.did.sdk.datasource.backup.content.microsoft2020.WalletMetadata
import com.microsoft.did.sdk.di.defaultTestSerializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Microsoft2020UnprotectedBackupDataTest {
    private val walletMetadata = WalletMetadata()

    private val vcMetadata = VcMetadata(
        VerifiableCredentialUtil.testDisplayContract
    )

    private val backup = Microsoft2020UnprotectedBackupData(
        mapOf("test" to VerifiableCredentialUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(VerifiableCredentialUtil.rawIdentifier)
    )

    @Test
    fun `type field should match static test`() {
        assertEquals(Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE, backup.type, "types should match")
    }

    @Test
    fun vcsToIteratorTest() {
        val iterator = backup.vcsToIterator( defaultTestSerializer )
        assertTrue(iterator.hasNext())
        assertEquals(iterator.next(), Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    }
}