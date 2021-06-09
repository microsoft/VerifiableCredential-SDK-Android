// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.BackupTestUtil
import org.junit.Test
import kotlin.test.assertEquals

class Microsoft2020UnprotectedBackupDataTest {
    private val vcMetadata = TestVcMetaData(
        BackupTestUtil.testDisplayContract
    )
    private val walletMetadata = WalletMetadata()

    private val backup = Microsoft2020UnprotectedBackupData(
        mapOf("test" to BackupTestUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(BackupTestUtil.rawIdentifier)
    )

    @Test
    fun `type field should match static test`() {
        assertEquals(Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE, backup.type, "types should match")
    }
}