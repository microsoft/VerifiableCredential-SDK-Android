// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.VerifiableCredentialUtil
import org.junit.Test
import kotlin.test.assertEquals

class Microsoft2020UnprotectedBackupDataTest {
    private val vcMetadata = TestVcMetaData(
        VerifiableCredentialUtil.testDisplayContract
    )
    private val walletMetadata = WalletMetadata()

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
}