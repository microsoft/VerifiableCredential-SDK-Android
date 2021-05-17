// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class Microsoft2020UnprotectedBackupProcessorTest {
    private val identifierRepository = VerifiableCredentialUtil.getMockIdentifierRepository()
    private val keyStore = VerifiableCredentialUtil.getMockKeyStore()
    private val rawIdentifierUtility = RawIdentifierConverter(identifierRepository, keyStore)

    private val backupProcessor = Microsoft2020BackupProcessor(
        identifierRepository,
        keyStore,
        rawIdentifierUtility,
        defaultTestSerializer
    )

    private val vcMetadata = TestVcMetaData(VerifiableCredentialUtil.testDisplayContract)
    private val backupData = Microsoft2020UnprotectedBackup(
        WalletMetadata(),
        listOf(Pair(VerifiableCredentialUtil.testVerifiedCredential, vcMetadata))
    )

    @Test
    fun `import BackupData returns data and writes keys and identifiers`() {
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
            val actual = backupProcessor.import(rawData) as Microsoft2020UnprotectedBackup
            assertEquals(
                backupData.verifiableCredentials,
                actual.verifiableCredentials
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
    fun `transformToBackupData transforms backup correctly`() {
        runBlocking {
            val actual = backupProcessor.export(backupData) as Microsoft2020UnprotectedBackupData
            coVerify {
                identifierRepository.queryAllLocal()
            }
            assertEquals(1, actual.vcs.size)
            assertEquals(
                VerifiableCredentialUtil.testVerifiedCredential.raw,
                actual.vcs.values.first()
            )
            assertEquals(1, actual.vcsMetaInf.size)
            assertEquals(vcMetadata, actual.vcsMetaInf.values.first())
            assertEquals(1, actual.identifiers.size)
            assertEquals(
                VerifiableCredentialUtil.rawIdentifier.id,
                actual.identifiers.first().id
            )
        }
    }
}