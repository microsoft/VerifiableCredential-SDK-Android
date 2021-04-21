// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RawIdentiiferUtilityTest {
    private val expectedId = "did:example: ${Base64.getEncoder().encodeToString(Random.nextBytes(8))}"
    private val expectedName = Base64.getEncoder().encodeToString(Random.nextBytes(8))
    private val recoverKeyRef = "recover"
    private val updateKeyRef = "update"
    private val signKeyRef = "sign"
    private val encryptKeyRef = "encrypt"

    private val recoveryKey = mockk<JWK>()
    private val updateKey = mockk<JWK>()
    private val signatureKey = mockk<JWK>()
    private val encryptionKey = mockk<JWK>()
    private val identifierRepository = mockk<IdentifierRepository>()
    private val keyStore = mockk<EncryptedKeyStore>()

    init {
        every { recoveryKey.keyID } returns(recoverKeyRef)
        every { recoveryKey.toJSONObject() } returns(mapOf<String, String>("kid" to recoverKeyRef))
        every { keyStore.getKey(recoverKeyRef) } returns (recoveryKey)
        every { updateKey.keyID } returns(updateKeyRef)
        every { updateKey.toJSONObject() } returns(mapOf<String, String>("kid" to updateKeyRef))
        every { keyStore.getKey(updateKeyRef) } returns (updateKey)
        every { signatureKey.keyID } returns(signKeyRef)
        every { signatureKey.toJSONObject() } returns(mapOf<String, String>("kid" to signKeyRef))
        every { keyStore.getKey(signKeyRef) } returns (signatureKey)
        every { encryptionKey.keyID } returns(encryptKeyRef)
        every { encryptionKey.toJSONObject() } returns(mapOf<String, String>("kid" to encryptKeyRef))
        every { keyStore.getKey(encryptKeyRef) } returns (encryptionKey)
        mockkStatic(JWK::class)
        every { JWK.parse( allAny<Map<String, Any>>() ) } answers {
            val obj =  it.invocation.args.first() as Map<String, Any>
            val out = mockk<JWK>()

            // why is there a crazy amount of conversion code here? Because I don't want to bother JWK to use real keys.
            val kid = obj["kid"] as String?
            val use = when(obj["use"]) {
                "enc" -> KeyUse.ENCRYPTION
                "sig" -> KeyUse.SIGNATURE
                else -> null
            }
            val ops = (obj["key_ops"] as? List<String>)?.mapNotNull { op -> when (op) {
                KeyOperation.UNWRAP_KEY.identifier() -> KeyOperation.UNWRAP_KEY
                KeyOperation.WRAP_KEY.identifier() -> KeyOperation.WRAP_KEY
                KeyOperation.ENCRYPT.identifier() -> KeyOperation.ENCRYPT
                KeyOperation.DECRYPT.identifier() -> KeyOperation.DECRYPT
                KeyOperation.SIGN.identifier() -> KeyOperation.SIGN
                KeyOperation.VERIFY.identifier() -> KeyOperation.VERIFY
                KeyOperation.DERIVE_BITS.identifier() -> KeyOperation.DERIVE_BITS
                KeyOperation.DERIVE_KEY.identifier() -> KeyOperation.DERIVE_KEY
                else -> null
            } }?.toSet()
            every { out.keyID } returns(kid)
            every { out.keyUse } returns(use)
            every { out.keyOperations } returns(ops)
            out
        }
        coEvery { identifierRepository.queryByIdentifier(expectedId) } returns( Identifier(
            expectedId,
            signKeyRef,
            encryptKeyRef,
            recoverKeyRef,
            updateKeyRef,
            expectedName
        ))
    }

    @Test
    fun getIdentiferKeysTest() {

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