// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.di.defaultTestSerializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MicrosoftUnprotectedBackup2020Test {
    private val walletMetadata = WalletMetadata()

    private val vcMetadata = VCMetadata(
        VerifiableCredentialUtil.testDisplayContract
    )

    private val backup = MicrosoftUnprotectedBackup2020(
        mapOf("test" to VerifiableCredentialUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(VerifiableCredentialUtil.rawIdentifier)
    )

    @Test
    fun `type field should match static test`() {
        assertEquals(MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE, backup.type, "types should match")
    }

    @Test
    fun vcsToIteratorTest() {
        val iterator = backup.vcsToIterator( defaultTestSerializer )
        assertTrue(iterator.hasNext())
        assertEquals(iterator.next(), Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    }
}