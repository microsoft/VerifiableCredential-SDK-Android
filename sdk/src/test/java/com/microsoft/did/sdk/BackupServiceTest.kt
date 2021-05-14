// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.util.VerifiableCredentialUtil
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isInstanceOf
import com.microsoft.did.sdk.backup.BackupParser
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020BackupProcessor
import com.microsoft.did.sdk.backup.content.microsoft2020.RawIdentifierConverter
import com.microsoft.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackup
import com.microsoft.did.sdk.backup.container.jwe.JwePasswordProtectedBackupData
import com.microsoft.did.sdk.backup.container.jwe.JwePasswordProtectionMethod
import com.microsoft.did.sdk.backup.content.BackupProcessorFactory
import com.microsoft.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.did.sdk.backup.content.microsoft2020.WalletMetadata
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.defaultTestSerializer
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackupServiceTest {
    private val identifierRepository = VerifiableCredentialUtil.getMockIdentifierRepository()
    private val keyStore = VerifiableCredentialUtil.getMockKeyStore()

    private val jweBackupFactory = BackupParser()
    private val microsoft2020BackupProcessor = Microsoft2020BackupProcessor(
        identifierRepository,
        keyStore,
        RawIdentifierConverter(identifierRepository, keyStore),
        defaultTestSerializer
    )
    private val backupProcessorFactory = BackupProcessorFactory(microsoft2020BackupProcessor)

    private val service = BackupService(jweBackupFactory, backupProcessorFactory, defaultTestSerializer)
    private val password = "Big complex passsword you'll never be able to guess"

    private val vcMetadata = VcMetadata(VerifiableCredentialUtil.testDisplayContract)
    private val backup = Microsoft2020UnprotectedBackup(
        WalletMetadata(),
        listOf(Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    )

    @Test
    fun `export and import returns protectedBackupData`() {
        runBlocking {
            val protectionMethod = JwePasswordProtectionMethod(password)
            val protectedBackupData = (service.exportBackup(backup, protectionMethod) as Result.Success).payload
            assertThat(protectedBackupData).isInstanceOf(JwePasswordProtectedBackupData::class)

            val actualBackup = service.importBackup(protectedBackupData, protectionMethod)
            assertThat(actualBackup).isDataClassEqualTo(backup)
        }
    }

    @Test
    fun `parse backup returns backup`() {

    }

    @Test
    fun `parse backup returns Failure for bad String`() {
        runBlocking {
            assertThat(service.parseBackup("ASDF")).isInstanceOf(Result.Failure::class)
        }
    }
}