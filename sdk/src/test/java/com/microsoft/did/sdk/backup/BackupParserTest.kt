// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup

import com.microsoft.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.did.sdk.backup.content.microsoft2020.WalletMetadata
import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.backup.container.jwe.JwePasswordProtectedBackupData
import com.microsoft.did.sdk.backup.container.jwe.JwePasswordProtectionMethod
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.util.defaultTestSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BackupParserTest {

    private val vcMetadata = VcMetadata(
        VerifiableCredentialUtil.testDisplayContract
    )
    private val walletMetadata = WalletMetadata()
    private val backup = Microsoft2020UnprotectedBackupData(
        mapOf("test" to VerifiableCredentialUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(VerifiableCredentialUtil.rawIdentifier)
    )
    private val backupParser = BackupParser()


    @Test
    fun `test properly wraps, serializes, parses and unwraps`() {
        val protectionMethod = JwePasswordProtectionMethod("foo")
        val expectedProtectedBackupData = protectionMethod.wrap(backup, defaultTestSerializer)
        val serializedBackup = expectedProtectedBackupData.serialize()

        val actualProtectedBackupData = backupParser.parseBackup(serializedBackup)
        assertThat(actualProtectedBackupData).isInstanceOf(JwePasswordProtectedBackupData::class.java)

        val actualUnprotectedBackup = protectionMethod.unwrap(actualProtectedBackupData, defaultTestSerializer)
        assertThat(actualUnprotectedBackup).isInstanceOf(Microsoft2020UnprotectedBackupData::class.java)
        assertThat(actualUnprotectedBackup).isEqualToComparingFieldByFieldRecursively(backup)
    }
}