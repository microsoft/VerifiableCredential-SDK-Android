// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.stringToByteArray
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

@RunWith(AndroidJUnit4ClassRunner::class)
class IdentifierCreatorInstrumentedTest {

    private val cryptoOperations: CryptoOperations
    private val androidSubtle: SubtleCrypto
    private val ecSubtle: EllipticCurveSubtleCrypto
    private val identifierCreator: IdentifierCreator
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        VerifiableCredentialSdk.init(context, "testAgent")
        val keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle, defaultTestSerializer)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        cryptoOperations = CryptoOperations(androidSubtle, keyStore, ellipticCurvePairwiseKey)
        val sidetreePayloadProcessor = SidetreePayloadProcessor(defaultTestSerializer)
        identifierCreator = IdentifierCreator(cryptoOperations, sidetreePayloadProcessor)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.ALL)
        )
    }

    @Test
    fun idCreationTest() {
        runBlocking {
            val id = identifierCreator.create("ION")
            if (id is Result.Success)
                assertThat(id.payload.name).isEqualTo(Constants.MASTER_IDENTIFIER_NAME)
        }
    }

    @Test
    fun pairwiseIdCreationTest() {
        runBlocking {
            cryptoOperations.generateAndStoreSeed()
            val personaDid = identifierCreator.create("ION")
            var personaId = ""
            if (personaDid is Result.Success)
                personaId = personaDid.payload.id

            val peerId =
                "did:ion:EiBiTB61bYBPooTMNwhP__A6IiBG1CQ77Cxv-xCL6_ewlg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlBZkp2c25ZbHoyMHMzMm5yNFRQcGd4WE40LXl4aUJtQ1JGRVFoUEpwTWhMdyIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJhZG1iSE1jMWxSTlFFelIyd0FwVGh4djRjdFdpUnp2eW5YSGNFMWlLUjhFIiwieSI6IjNaSmRZclNBNEpqQ3F5cWphTGQ0Q3d4a0xnN3R5UUgwSWNucXdOenQ4NDgifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpQkNhazJnV2tiaFVSVXdDM05aWWxMQjZHa2xyUFRON29sOWVVdHRHd1o5V0EifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRFZnUm01VU1oSVpIdmlVOW55Z3hiUFRQajJFUGhOcHhrTTB1Z1dDQTU3QUEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJseHNfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoidC1TRXVLd2dlWEh0c0ZBTkE0TTRZSlhtajZXdVUwX1NNbXdxZ1VwaHFxbyIsInkiOiJHR19lMlRqMkhpNUJ2cHk3NVpDX2ZQQlFBMllDdmJxeWNNRVRTMjZhTEJ3In0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"
            val pairwiseId = identifierCreator.createPairwiseId(personaId, peerId)
            val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
            val expectedPairwiseDidName = Base64Url.encode(digest.digest(stringToByteArray(peerId)))
            if (pairwiseId is Result.Success)
                assertThat(pairwiseId.payload.name).isEqualTo(expectedPairwiseDidName)
        }
    }

    // This test fails during compile time on pipeline but passes on local builds. Commenting it out for now until we figure out a solution for it.
/*    @Test
    fun signAndVerifyTest() {
        val test = "test string"
        val testPayload = test.toByteArray()
        var signKey = ""
        runBlocking {
            signKey =
                when (val id = VerifiableCredentialSdk.identifierManager.getMasterIdentifier()) {
                    is Result.Success -> id.payload.signatureKeyReference
                    else -> ""
                }
        }

        val token = JwsToken(testPayload, defaultTestSerializer)
        token.sign(signKey, cryptoOperations)
        assertThat(token.signatures).isNotNull
        val publicKeys: List<PublicKey> =
            when (val publicKey = cryptoOperations.keyStore.getPublicKeyById("#${signKey}_1")) {
                null -> emptyList()
                else -> listOf(publicKey)
            }
        val matched = token.verify(cryptoOperations, publicKeys)
        assertThat(matched).isTrue()
    }*/
}