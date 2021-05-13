// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020Backup
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.VcMetadata
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.WalletMetadata
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.MicrosoftBackupSerializer
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MicrosoftBackupSerializerTest {
    private val identifierRepository = VerifiableCredentialUtil.getMockIdentifierRepository()
    private val keyStore = VerifiableCredentialUtil.getMockKeyStore()
    private val rawIdentifierUtility = RawIdentifierConverter(identifierRepository, keyStore)
    private val microsoftBackupSerializer = MicrosoftBackupSerializer(
        identifierRepository,
        keyStore,
        rawIdentifierUtility,
        defaultTestSerializer
    )

    private val vcMetadata = VcMetadata(VerifiableCredentialUtil.testDisplayContract)
    private val backupData = Microsoft2020Backup(
        WalletMetadata(),
        listOf(Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    )

    @Test
    fun importTest() {
        runBlocking {
            val rawData = Microsoft2020UnprotectedBackupData(
                mapOf(
                    "test" to VerifiableCredentialUtil.testVerifiedCredential.raw,
                ),
                mapOf(
                    "test" to vcMetadata,
                ),
                WalletMetadata(),
                listOf(
                    VerifiableCredentialUtil.rawIdentifier
                )
            )
            val actual = microsoftBackupSerializer.import( rawData )
            assertEquals(
                backupData.verifiableCredentials,
                actual.verifiableCredentials
            )
            assertTrue(
                actual.walletMetadata is WalletMetadata
            )
            verify {
                keyStore.containsKey(VerifiableCredentialUtil.signKey.keyID)
                keyStore.containsKey(VerifiableCredentialUtil.encryptKey.keyID)
                keyStore.containsKey(VerifiableCredentialUtil.recoverKey.keyID)
                keyStore.containsKey(VerifiableCredentialUtil.updateKey.keyID)
            }
            coVerify {
                identifierRepository.insert(VerifiableCredentialUtil.testIdentifer)
            }
        }
    }

    @Test
    fun createTest() {
        runBlocking {
            val actual = microsoftBackupSerializer.create(backupData)
            coVerify {
                identifierRepository.queryAllLocal()
            }
            assertTrue(actual.metaInf is WalletMetadata)
            assertEquals(1, actual.vcs.size)
            assertEquals(VerifiableCredentialUtil.testVerifiedCredential.raw,
                actual.vcs.values.first())
            assertEquals(1, actual.vcsMetaInf.size)
            assertEquals(vcMetadata, actual.vcsMetaInf.values.first())
            assertEquals(1, actual.identifiers.size)
            assertEquals(VerifiableCredentialUtil.rawIdentifier.id,
                actual.identifiers.first().id)
        }
    }
}