// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import android.util.VerifiableCredentialUtil
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Base64
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RawIdentiiferUtilityTest {
    private val identifierRepository = VerifiableCredentialUtil.getMockIdentifierRepository()
    private val keyStore = VerifiableCredentialUtil.getMockKeyStore()
    private val rawIdentifierUtility = RawIdentifierUtility(identifierRepository, keyStore)

    @Test
    fun parseRawIdentifierTest() {
        val actual = rawIdentifierUtility.parseRawIdentifier(VerifiableCredentialUtil.rawIdentifier)
        assertEquals(VerifiableCredentialUtil.testIdentifer, actual.first)
        assertEquals(4, actual.second.size, "expected four distinct keys")
        assertTrue(actual.second.contains(VerifiableCredentialUtil.encryptKey))
        assertTrue(actual.second.contains(VerifiableCredentialUtil.signKey))
        assertTrue(actual.second.contains(VerifiableCredentialUtil.updateKey))
        assertTrue(actual.second.contains(VerifiableCredentialUtil.recoverKey))
    }

    @Test
    fun getAllIdentifiersTest() {
        runBlocking {
            val actual = rawIdentifierUtility.getAllIdentifiers()
            assertEquals(1, actual.size, "expected one DID")
            assertEquals(VerifiableCredentialUtil.rawIdentifier.id, actual[0].id)
        }
    }

//    @Test
//    fun didToRawIdentifierTest() {
//        runBlocking {
//            val result = RawIdentifierUtility.didToRawIdentifier(expectedId, identifierRepository, keyStore)
//            assertNotNull(result, "Failed to form rawIdentifier")
//            assertEquals(expectedId, result.id, "DID does not match")
//            assertEquals(expectedName, result.name, "Name does not match")
//            assertEquals(recoverKeyRef, result.recoveryKey, "recovery key id does not match")
//            assertEquals(updateKeyRef, result.updateKey, "update key id does not match")
//            result.keys.forEach {
//                when (it.keyID) {
//                    recoverKeyRef,
//                    updateKeyRef -> {
//                        // do nothing, we've checked above
//                    }
//                    signKeyRef -> {
//                        val correctUse = it.keyUse == KeyUse.SIGNATURE
//                        val correctOps = it.keyOperations?.contains(KeyOperation.SIGN)
//                        assertTrue(correctOps == true || correctUse, "Did not find correct key_ops or use")
//                    }
//                    encryptKeyRef -> {
//                        val correctUse = it.keyUse == KeyUse.ENCRYPTION
//                        val correctOps = it.keyOperations?.contains(KeyOperation.UNWRAP_KEY)
//                        assertTrue(correctOps == true || correctUse, "Did not find correct key_ops or use")
//                    }
//                    else ->
//                        Assert.fail("Unexpected Key found: ${it.keyID}")
//                }
//            }
//            assertEquals(4, result.keys.count(), "Expected for distinct keys")
//        }
//    }
}