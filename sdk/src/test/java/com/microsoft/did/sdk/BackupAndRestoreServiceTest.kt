// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

// only used for type
import android.content.Context
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory
import com.microsoft.did.sdk.datasource.file.JweProtectedBackupFactory_Factory
import com.microsoft.did.sdk.datasource.file.MicrosoftBackupSerializer
import com.microsoft.did.sdk.util.DifWordList
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class BackupAndRestoreServiceTest {
    private val jweBackupFactory: JweProtectedBackupFactory = mockk()
    private val microsoftBackupSerializer: MicrosoftBackupSerializer = mockk()
    private val service = BackupAndRestoreService(jweBackupFactory, microsoftBackupSerializer, Json.Default)


    @Test
    fun createPasswordBackupTest() {
        fail("Not implemented")
    }
}