// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup

import com.microsoft.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.did.sdk.backup.content.microsoft2020.WalletMetadata
import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupParserTest {
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

    private val backupParser = BackupParser()

    @Test
    fun parseBackupTest() {
        val password = "foobarbaz"
        val backup = backupParser.createPasswordBackup(
            backup, password
        )
        val outputData = ByteArrayOutputStream()
        backupParser.writeOutput(backup, outputData)
        assertTrue(outputData.size() > 0)
        val inputData = ByteArrayInputStream(outputData.toByteArray())
        val actual = backupParser.parseBackup(inputData)
        assertEquals(backup.jweToken.serialize(), actual.jweToken.serialize())
    }
}