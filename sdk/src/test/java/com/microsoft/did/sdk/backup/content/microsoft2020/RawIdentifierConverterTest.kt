// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import android.util.BackupTestUtil
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class RawIdentifierConverterTest {
    private val identifierRepository = BackupTestUtil.getMockIdentifierRepository()
    private val keyStore = BackupTestUtil.getMockKeyStore()
    private val rawIdentifierUtility = RawIdentifierConverter(identifierRepository, keyStore)

    @Test
    fun parseRawIdentifierTest() {
        val actual = rawIdentifierUtility.parseRawIdentifier(BackupTestUtil.rawIdentifier)
        assertEquals(BackupTestUtil.testIdentifer, actual.first)
        assertEquals(4, actual.second.size, "expected four distinct keys")
        assertTrue(actual.second.contains(BackupTestUtil.encryptKey))
        assertTrue(actual.second.contains(BackupTestUtil.signKey))
        assertTrue(actual.second.contains(BackupTestUtil.updateKey))
        assertTrue(actual.second.contains(BackupTestUtil.recoverKey))
    }

    @Test
    fun getAllIdentifiersTest() {
        runBlocking {
            val actual = rawIdentifierUtility.getAllIdentifiers()
            assertEquals(1, actual.size, "expected one DID")
            assertEquals(BackupTestUtil.rawIdentifier.id, actual[0].id)
            validateRawIdentifier(actual[0])
        }
    }

    private fun validateRawIdentifier(rawIdentity: RawIdentity?) {
        val expected = BackupTestUtil.rawIdentifier
        assertNotNull(rawIdentity, "Failed to form rawIdentifier")
        assertEquals(expected.id, rawIdentity.id, "DID does not match")
        assertEquals(expected.name, rawIdentity.name, "Name does not match")
        assertEquals(expected.recoveryKey, rawIdentity.recoveryKey, "recovery key id does not match")
        assertEquals(expected.updateKey, rawIdentity.updateKey, "update key id does not match")
        rawIdentity.keys.forEach {
            when (it.keyID) {
                BackupTestUtil.recoverKey.keyID,
                BackupTestUtil.updateKey.keyID -> {
                    // do nothing, we've checked above
                }
                BackupTestUtil.signKey.keyID -> {
                    val correctUse = it.keyUse == KeyUse.SIGNATURE
                    val correctOps = it.keyOperations?.contains(KeyOperation.SIGN)
                    assertTrue(correctOps == true || correctUse, "Did not find correct key_ops or use")
                }
                BackupTestUtil.encryptKey.keyID -> {
                    val correctUse = it.keyUse == KeyUse.ENCRYPTION
                    val correctOps = it.keyOperations?.contains(KeyOperation.UNWRAP_KEY)
                    assertTrue(correctOps == true || correctUse, "Did not find correct key_ops or use")
                }
                else ->
                    fail("Unexpected Key found: ${it.keyID}")
            }
        }
        assertEquals(4, rawIdentity.keys.count(), "Expected for distinct keys")
    }
}