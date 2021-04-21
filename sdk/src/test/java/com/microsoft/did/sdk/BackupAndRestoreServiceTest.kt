// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

// only used for type
import android.content.Context
import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory_Factory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.datasource.file.RawIdentifierUtility
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020Data
import com.microsoft.did.sdk.datasource.file.models.PasswordEncryptedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackupData
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.util.DifWordList
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.andThen
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class BackupAndRestoreServiceTest {
    private val identifierRepository = VerifiableCredentialUtil.getMockIdentifierRepository()
    private val keyStore = VerifiableCredentialUtil.getMockKeyStore()

    private val jweBackupFactory = JweProtectedBackupFactory(defaultTestSerializer)
    private val microsoftBackupSerializer = MicrosoftBackupSerializer(
        identifierRepository,
        keyStore,
        RawIdentifierUtility(identifierRepository, keyStore),
        defaultTestSerializer
    )
    private val service = BackupAndRestoreService(jweBackupFactory, microsoftBackupSerializer, defaultTestSerializer)
    private val password = "Big complex passsword you'll never be able to guess"

    private val vcMetadata = VCMetadata(VerifiableCredentialUtil.testDisplayContract)
    private val backupData = MicrosoftBackup2020Data(
        WalletMetadata(),
        listOf(Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    )

    suspend fun createBackup(): JweProtectedBackup? {
        val encBackup = service.createBackup(PasswordProtectedBackupData(
            password,
            unprotectedBackup = backupData
        ))
        return if (encBackup is Result.Success) {
            encBackup.payload
        } else {
            null
        }
    }

    @Test
    fun createBackupTest() {
        runBlocking {
            val actual = service.createBackup(PasswordProtectedBackupData(
                password,
                unprotectedBackup = backupData
            ))
            assertTrue(actual is Result.Success)
            assertTrue(actual.payload.jweToken.serialize().isNotBlank())
        }
    }

    @Test
    fun writeBackupTest() {
        runBlocking {
            val encBackup = createBackup() ?: fail("Failed to create backup")
            val outputData = ByteArrayOutputStream()
            val actual = service.writeBackup(encBackup, outputData)
            assertTrue(outputData.size() > 0)
            assertTrue(actual is Result.Success)
        }
    }

    @Test
    fun parseBackupTest() {
        runBlocking {
            val encBackup = createBackup() ?: fail("Failed to create backup")
            val outputData = ByteArrayOutputStream()
            jweBackupFactory.writeOutput(encBackup, outputData)
            assertTrue(outputData.size() > 0)
            val inputData = ByteArrayInputStream(outputData.toByteArray())
            val actual = service.parseBackup(backupFile = inputData)
            assertTrue(actual is Result.Success)
            assertTrue(actual.payload.jweToken.serialize().isNotBlank())
        }
    }

    @Test
    fun restoreBackupTest() {
        runBlocking {
            val encBackup = createBackup() ?: fail("Failed to create backup")
            val actual = service.restoreBackup(
                PasswordEncryptedBackupData(
                    password,
                    backup = encBackup as PasswordProtectedBackup
                )
            )
            assertTrue(actual is Result.Success)
            assertTrue(actual.payload is MicrosoftBackup2020Data)
        }
    }
}