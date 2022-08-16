// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.BackupTestUtil
import com.microsoft.did.sdk.IdentifierService
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class Microsoft2020UnprotectedBackupProcessorTest {
    private val identifierRepository = BackupTestUtil.getMockIdentifierRepository()
    private val keyStore = BackupTestUtil.getMockKeyStore()
    private val rawIdentifierUtility = RawIdentifierConverter(identifierRepository, keyStore)
    private val identifierService: IdentifierService = mockk()
    private val masterIdentifier: Identifier = mockk()

    private val backupProcessor = Microsoft2020BackupProcessor(
        identifierService,
        identifierRepository,
        keyStore,
        rawIdentifierUtility,
        defaultTestSerializer
    )

    private val vcMetadata = TestVcMetaData(BackupTestUtil.testDisplayContract)
    private val backupData = Microsoft2020UnprotectedBackup(
        WalletMetadata(),
        listOf(Pair(BackupTestUtil.testVerifiedCredential, vcMetadata))
    )

    @Test
    fun `import BackupData returns data and writes keys and identifiers`() {
        runBlocking {
            val rawData = Microsoft2020UnprotectedBackupData(
                mapOf(
                    "test" to BackupTestUtil.testVerifiedCredential.raw
                ),
                mapOf(
                    "test" to vcMetadata
                ),
                WalletMetadata(),
                listOf(
                    BackupTestUtil.rawIdentifier
                )
            )
            rawData.metaInf.seed = "{\"kty\":\"EC\",\"d\":\"7-RR2JBX1tWCUw17ujvjhYFyB0zwkvM9ttKyBMfQUNM\",\"use\":\"sig\",\"crv\":\"P-256\"," +
                "\"kid\":\"recover\",\"x\":\"XIpoCl3CxnSUfZXew2Gc1tSwCJBUos1EacEXnQyfjhg\",\"y\":\"YDHaAGK0rL7wZwp2vp4aCaKuzg_tSB-8i4q_u-kLxSw\"}"
            val actual = backupProcessor.import(rawData) as Microsoft2020UnprotectedBackup
            assertEquals(
                backupData.verifiableCredentials,
                actual.verifiableCredentials
            )
            verify {
                keyStore.containsKey(BackupTestUtil.signKey.keyID)
                keyStore.containsKey(BackupTestUtil.encryptKey.keyID)
                keyStore.containsKey(BackupTestUtil.recoverKey.keyID)
                keyStore.containsKey(BackupTestUtil.updateKey.keyID)
            }
            coVerify {
                identifierRepository.insert(BackupTestUtil.testIdentifer)
            }
        }
    }

    @Test
    fun `export transforms backup correctly`() {
        coEvery { identifierService.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        runBlocking {
            val actual = backupProcessor.export(backupData) as Microsoft2020UnprotectedBackupData
            coVerify {
                identifierRepository.queryAllLocal()
            }
            assertEquals(1, actual.vcs.size)
            assertEquals(
                BackupTestUtil.testVerifiedCredential.raw,
                actual.vcs.values.first()
            )
            assertEquals(1, actual.vcsMetaInf.size)
            assertEquals(vcMetadata, actual.vcsMetaInf.values.first())
            assertEquals(1, actual.identifiers.size)
            assertEquals(
                BackupTestUtil.rawIdentifier.id,
                actual.identifiers.first().id
            )
        }
    }
}