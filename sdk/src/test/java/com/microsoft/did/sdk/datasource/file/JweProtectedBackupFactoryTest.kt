// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.datasource.file.models.VcMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackupData2020
import com.microsoft.did.sdk.util.defaultTestSerializer
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JweProtectedBackupFactoryTest {
    private val walletMetadata = WalletMetadata()

    private val vcMetadata = VcMetadata(
        VerifiableCredentialUtil.testDisplayContract
    )

    private val backup = MicrosoftUnprotectedBackupData2020(
        mapOf("test" to VerifiableCredentialUtil.testVerifiedCredential.raw),
        mapOf("test" to vcMetadata),
        walletMetadata,
        listOf(VerifiableCredentialUtil.rawIdentifier)
    )

    val jweProtectedBackupFactory = JweProtectedBackupFactory(defaultTestSerializer)

    @Test
    fun createPasswordBackupTest() {
        val password = "foobarbaz"
        val actual = jweProtectedBackupFactory.createPasswordBackup(
            backup, password
        )
        assertTrue(actual.jweToken.contentAsString.isNotBlank())
    }

    @Test
    fun parseBackupTest() {
        val password = "foobarbaz"
        val backup = jweProtectedBackupFactory.createPasswordBackup(
            backup, password
        )
        val outputData = ByteArrayOutputStream()
        jweProtectedBackupFactory.writeOutput(backup, outputData)
        assertTrue(outputData.size() > 0)
        val inputData = ByteArrayInputStream(outputData.toByteArray())
        val actual = jweProtectedBackupFactory.parseBackup(inputData)
        assertEquals(backup.jweToken.serialize(), actual.jweToken.serialize())
    }
}