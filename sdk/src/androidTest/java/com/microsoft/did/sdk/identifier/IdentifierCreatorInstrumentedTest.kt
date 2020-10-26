// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.LinkedDomainsService
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.stringToByteArray
import io.mockk.mockk
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
    private val mockedJwtDomainLinkageCredentialValidator: JwtDomainLinkageCredentialValidator
    private val linkedDomainsService: LinkedDomainsService

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        VerifiableCredentialSdk.init(context, "")
        val serializer = Serializer()
        val keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle, serializer)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        cryptoOperations = CryptoOperations(androidSubtle, keyStore, ellipticCurvePairwiseKey)
        val sidetreePayloadProcessor = SidetreePayloadProcessor(serializer)
        identifierCreator = IdentifierCreator(cryptoOperations, sidetreePayloadProcessor)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.ALL)
        )
        val resolver = Resolver("https://beta.discover.did.microsoft.com/1.0/identifiers", VerifiableCredentialSdk.identifierManager.identifierRepository)
        val jwtValidator = JwtValidator(cryptoOperations, resolver, serializer)
        mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(jwtValidator, serializer)
        linkedDomainsService = LinkedDomainsService(mockk(relaxed = true), resolver, mockedJwtDomainLinkageCredentialValidator)
    }


    @Test
    fun `testfetchAndVerify`() {
        val suppliedDidWithoutServiceEndpoint =
            "did:ion:EiAGYVovJcSCiUWuX9K1eFHBcv4BorIjMG7e44hf1hKtGg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDUDBKOEVyRmZXeEw2WGNqT2g4STU2Smp3bXhVQ01zWk5yT2ZoSWFMbUxVQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUQyNHVWbkd1ZW9aZUs0OEl1aE9BZ1c4Z3NvTmdncHV2bGRRSUVjM09wNFZRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXpYZmprQkE1Z05tZWJOam56TmhkYzYycjdCUkJremcyOXFLWVBON3MtQUEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoiY2FwcHRvc28taXNzdWVyLXNpdGUtc2lnbmluZy1rZXkiLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5IiwiandrIjp7ImtpZCI6Imh0dHBzOi8vdmMtMjAyMC1rdi52YXVsdC5henVyZS5uZXQva2V5cy9jYXBwdG9zby1pc3N1ZXItc2l0ZS1zaWduaW5nLWtleS9lZTM5MDUxNGFhN2Y0ZjNiYTAzZjViNDM3ZjNlYjRlZSIsImt0eSI6IkVDIiwiY3J2Ijoic2VjcDI1NmsxIiwieCI6IkFIQ29XM1k4cHVvRmFqa0JqeU1HcUtwZTJ3TktFb1BaSWtINDVxelJaeVUiLCJ5IjoiQ3JaaU02VU1sLVFNMnlCYTgtaS1kTlM3X1JyeDA3VnN3OVlTVlA4UzBxTSJ9LCJwdXJwb3NlIjpbImF1dGgiLCJnZW5lcmFsIl19XX19XX0"
        runBlocking {
            val domain = linkedDomainsService.fetchAndVerifyLinkedDomains(suppliedDidWithoutServiceEndpoint)
            assertThat(domain).isInstanceOf(Result.Success::class.java)
            assertThat((domain as Result.Success).payload).isInstanceOf(LinkedDomainResult.UnVerified::class.java)
        }
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

    @Test
    fun signAndVerifyTest() {
        val serializer = Serializer()
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

        val token = JwsToken(testPayload, serializer)
        token.sign(signKey, cryptoOperations)
        assertThat(token.signatures).isNotNull
        val publicKeys: List<PublicKey> =
            when (val publicKey = cryptoOperations.keyStore.getPublicKeyById("#${signKey}_1")) {
                null -> emptyList()
                else -> listOf(publicKey)
            }
        val matched = token.verify(cryptoOperations, publicKeys)
        assertThat(matched).isTrue()
    }
}