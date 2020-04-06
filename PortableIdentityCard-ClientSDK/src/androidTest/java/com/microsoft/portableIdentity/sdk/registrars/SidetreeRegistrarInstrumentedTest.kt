// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.registrars

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.PortableIdentitySdk
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.*
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.resolvers.HttpResolver
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class SidetreeRegistrarInstrumentedTest {

    private val registrar: Registrar
    private val resolver: Resolver
    private val cryptoOperations: CryptoOperations
    private val androidSubtle: SubtleCrypto
    private val ecSubtle: EllipticCurveSubtleCrypto

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        PortableIdentitySdk.init(context)
        val keyStore = AndroidKeyStore(context)
        androidSubtle = AndroidSubtle(keyStore)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle)
        registrar = SidetreeRegistrar("http://10.91.6.163:3000")
        resolver = HttpResolver("http://10.91.6.163:3000", PortableIdentitySdk.identityManager.identityRepository)
        cryptoOperations = CryptoOperations(androidSubtle, keyStore)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

/*    private fun hash(bytes: ByteArray): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        return messageDigest.digest(bytes)
    }*/

/*    @Test
    fun longFormCreationTest() {
        var identifierDocumentPayload: IdentifierDocumentPayload = IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKey(
                    id = "#key1",
                    type = "Secp256k1VerificationKey2018",
                    publicKeyHex = "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
                )
            )
        )
        val identifierDocumentPatch = IdentifierDocumentPatch("replace", identifierDocumentPayload)
        val nextUpdateOtp = Base64Url.encode(Random.Default.nextBytes(32))
        val nextUpdateOtpHash = byteArrayOf(18, 32) + hash(stringToByteArray(nextUpdateOtp))
        val nextUpdateOtpHashString = Base64Url.encode(nextUpdateOtpHash)

        val operationData = OperationData(nextUpdateOtpHashString, listOf(identifierDocumentPatch))
        val opDataJson = Serializer.stringify(OperationData.serializer(), operationData)
        val opDataByteArray = stringToByteArray(opDataJson)
        val opDataHashed = byteArrayOf(18, 32) + hash(opDataByteArray)
        val opDataHash = Base64Url.encode(opDataHashed)
        val opDataEncodedString = Base64Url.encode(opDataByteArray)

        val nextRecoveryOtp = Base64Url.encode(Random.Default.nextBytes(32))
        val nextRecoveryOtpHash = byteArrayOf(18, 32) + hash(stringToByteArray(nextRecoveryOtp))
        val nextRecoveryOtpHashString = Base64.encode(nextRecoveryOtpHash)

        val suffixData = SuffixData(
            opDataHash,
            RecoveryKey("03f513461b26cfeb508c79ae884f1090e8e431d06bbc6ae52eea31fd381bc52fa5"),
            nextRecoveryOtpHashString
        )
        val suffixDataJson = Serializer.stringify(SuffixData.serializer(), suffixData)
        val suffixDataEncodedString = Base64Url.encode(stringToByteArray(suffixDataJson))
        val suffixDataHashed = byteArrayOf(18, 32) + hash(stringToByteArray(suffixDataJson))
        val uniqueSuffix = Base64Url.encode(suffixDataHashed)
        val did = "did:ion:test:$uniqueSuffix"

        val regDoc = RegistrationDocument("create", suffixDataEncodedString, opDataEncodedString)
        val regDocJson = Serializer.stringify(RegistrationDocument.serializer(), regDoc)
        val regDocEncodedString = Base64Url.encode(stringToByteArray(regDocJson))

        println("did is $did and initial-state is $regDocEncodedString")
        runBlocking {
            val identifier = resolver.resolve(did, regDocEncodedString, cryptoOperations)
            val resolvedIdentifierDocument = identifier.document
            assertThat(resolvedIdentifierDocument).isNotNull
        }
    }*/

/*    @Test
    fun idCreationTest() {
        val alias = Base64Url.encode(Random.nextBytes(5))
        runBlocking {
            val id = Identifier.createLongFormIdentifier(
                alias,
                cryptoOperations,
                "signatureKeyReference",
                "encryptionKeyReference",
                "recoveryKeyReference",
                resolver,
                registrar
            )
            assertThat(id).isNotNull
        }
    }*/
}