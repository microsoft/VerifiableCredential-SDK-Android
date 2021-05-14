// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.di.defaultTestSerializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Microsoft2020UnprotectedBackupDataTest {
    private class TestVcMetaData(override val displayContract: DisplayContract) : VcMetadata()

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