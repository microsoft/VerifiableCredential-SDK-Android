// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.DnsBindingService
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
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

    private val dnsBindingService: DnsBindingService
    val serializer = Serializer()
    val resolver: Resolver
    val jwtDomainLinkageCredentialValidator: JwtDomainLinkageCredentialValidator

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        VerifiableCredentialSdk.init(context)
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
        val identifierRepository = VerifiableCredentialSdk.identifierManager.identifierRepository
        resolver = Resolver("https://beta.discover.did.microsoft.com/1.0/identifiers", identifierRepository)
        val jwtValidator = JwtValidator(cryptoOperations, resolver, serializer)
        jwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(jwtValidator, serializer)
        dnsBindingService = DnsBindingService(identifierRepository.apiProvider, resolver, jwtDomainLinkageCredentialValidator)

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

/*    @Test
    fun getServiceEndpoints() {
        val suppliedDid = "did:ion:EiAkfaAsQG7FageU5GQtIcVI73AphEyFydjRWVj-2Ww7dw?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDQnZNU2RqTGRyZkNCZ1NjeXoyNFFkSmZnYXR5UGN1Y0dXeEJZaldvOEN5QSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsVWViZTVsalJLVkV6bUhabjNNTkpkOWdNcXV4VzJDb0N3bG1TSmxSVmpnIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQUVnYTA5bUdwWWJVTjBiampCZGMxTjdGY2M0NHhtWTdVdkZqRDRZSjZRanciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnbiIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJ1c2UiOiJzaWciLCJhbGciOiJFUzI1NksiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiMWI5ZDJXWEx5Mm81V3JqVUtJQTRHRHR2QjcxQmlsdG5aRS03ZGQ5SzFSUSIsInkiOiJxVk1ZX19yOVpTWV9CeTZmSFFQN0R4c184c2k5bVNRelUtdENiWG50VW1VIn0sInB1cnBvc2UiOlsiYXV0aCIsImdlbmVyYWwiXX1dfX1dfQ"
        runBlocking {
            val serviceEndpoints = dnsBindingService.getDomainForRp(suppliedDid)
            assertThat(serviceEndpoints).isEmpty()
        }
    }*/

    @Test
    fun `validateConfigDoc`() {
        val docJwt =
            """{
    "@context": "https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld",
    "linked_dids": [
        "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQjdKLWswZjNJbUctenlGVUZSbVY0Z2FPc2Rmankza2RkSl9kOU9PdjdzaEE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1psQlVXakZZYlRSNFRtTTVOblpRY25kdldUQTFia3hmTkRWWlVHMTViMVZuYjNsU2FrRkxRVTFrWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVGRlRrMDBjRTVMWWtaNlUyWXhSazFZUkU1c1lUQlZObEZDVkcxclkxQkhPRTF3VEdOUlpGWlBUa3RuSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUWkxRFRUVTFTRjkyTjB4YVFXOUNXRWsyT1haRWNuQTFYMmQwWDJwdFF6SnVibGhDTFZVNVRsUnhWMmNpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5Yek0zTXpkbU1qSTJJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKc2NrNVBZMTh5YjNSak0zWlVlRWx1WlRCbGNraFVVeTFHYkdGNE9VSnJRMkZXZWtKTFZGVmZaMkpuSWl3aWVTSTZJblZVTkZkNGJubFJZVnAzTFRSNlRsaEJkVE0wZGxscVNuWjNaRmh1WkRKNFdqZHhWbVZoYW1kMGMwVWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnXzM3MzdmMjI2In0.eyJzdWIiOiJkaWQ6aW9uOkVpQjdKLWswZjNJbUctenlGVUZSbVY0Z2FPc2Rmankza2RkSl9kOU9PdjdzaEE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1psQlVXakZZYlRSNFRtTTVOblpRY25kdldUQTFia3hmTkRWWlVHMTViMVZuYjNsU2FrRkxRVTFrWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVGRlRrMDBjRTVMWWtaNlUyWXhSazFZUkU1c1lUQlZObEZDVkcxclkxQkhPRTF3VEdOUlpGWlBUa3RuSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUWkxRFRUVTFTRjkyTjB4YVFXOUNXRWsyT1haRWNuQTFYMmQwWDJwdFF6SnVibGhDTFZVNVRsUnhWMmNpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5Yek0zTXpkbU1qSTJJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKc2NrNVBZMTh5YjNSak0zWlVlRWx1WlRCbGNraFVVeTFHYkdGNE9VSnJRMkZXZWtKTFZGVmZaMkpuSWl3aWVTSTZJblZVTkZkNGJubFJZVnAzTFRSNlRsaEJkVE0wZGxscVNuWjNaRmh1WkRKNFdqZHhWbVZoYW1kMGMwVWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQjdKLWswZjNJbUctenlGVUZSbVY0Z2FPc2Rmankza2RkSl9kOU9PdjdzaEE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1psQlVXakZZYlRSNFRtTTVOblpRY25kdldUQTFia3hmTkRWWlVHMTViMVZuYjNsU2FrRkxRVTFrWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVGRlRrMDBjRTVMWWtaNlUyWXhSazFZUkU1c1lUQlZObEZDVkcxclkxQkhPRTF3VEdOUlpGWlBUa3RuSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUWkxRFRUVTFTRjkyTjB4YVFXOUNXRWsyT1haRWNuQTFYMmQwWDJwdFF6SnVibGhDTFZVNVRsUnhWMmNpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5Yek0zTXpkbU1qSTJJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKc2NrNVBZMTh5YjNSak0zWlVlRWx1WlRCbGNraFVVeTFHYkdGNE9VSnJRMkZXZWtKTFZGVmZaMkpuSWl3aWVTSTZJblZVTkZkNGJubFJZVnAzTFRSNlRsaEJkVE0wZGxscVNuWjNaRmh1WkRKNFdqZHhWbVZoYW1kMGMwVWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDI3MDUwMTYsInZjIjp7IkBjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIiwiaHR0cHM6Ly9pZGVudGl0eS5mb3VuZGF0aW9uLy53ZWxsLWtub3duL2NvbnRleHRzL2RpZC1jb25maWd1cmF0aW9uLXYwLjAuanNvbmxkIl0sImlzc3VlciI6ImRpZDppb246RWlCN0otazBmM0ltRy16eUZVRlJtVjRnYU9zZGZqeTNrZGRKX2Q5T092N3NoQT8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xDWmxCVVdqRlliVFI0VG1NNU5uWlFjbmR2V1RBMWJreGZORFZaVUcxNWIxVm5iM2xTYWtGTFFVMWtaeUlzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUZGVGswMGNFNUxZa1o2VTJZeFJrMVlSRTVzWVRCVk5sRkNWRzFyWTFCSE9FMXdUR05SWkZaUFRrdG5JbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRaTFEVFRVMVNGOTJOMHhhUVc5Q1dFazJPWFpFY25BMVgyZDBYMnB0UXpKdWJsaENMVlU1VGxSeFYyY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblh6TTNNemRtTWpJMklpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpzY2s1UFkxOHliM1JqTTNaVWVFbHVaVEJsY2toVVV5MUdiR0Y0T1VKclEyRldla0pMVkZWZloySm5JaXdpZVNJNkluVlVORmQ0Ym5sUllWcDNMVFI2VGxoQmRUTTBkbGxxU25aM1pGaHVaREo0V2pkeFZtVmhhbWQwYzBVaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsImlzc3VhbmNlRGF0ZSI6IjIwMjAtMTAtMTRUMTk6NTA6MTYuNzEwWiIsInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJEb21haW5MaW5rYWdlQ3JlZGVudGlhbCJdLCJjcmVkZW50aWFsU3ViamVjdCI6eyJpZCI6ImRpZDppb246RWlCN0otazBmM0ltRy16eUZVRlJtVjRnYU9zZGZqeTNrZGRKX2Q5T092N3NoQT8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xDWmxCVVdqRlliVFI0VG1NNU5uWlFjbmR2V1RBMWJreGZORFZaVUcxNWIxVm5iM2xTYWtGTFFVMWtaeUlzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUZGVGswMGNFNUxZa1o2VTJZeFJrMVlSRTVzWVRCVk5sRkNWRzFyWTFCSE9FMXdUR05SWkZaUFRrdG5JbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRaTFEVFRVMVNGOTJOMHhhUVc5Q1dFazJPWFpFY25BMVgyZDBYMnB0UXpKdWJsaENMVlU1VGxSeFYyY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblh6TTNNemRtTWpJMklpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpzY2s1UFkxOHliM1JqTTNaVWVFbHVaVEJsY2toVVV5MUdiR0Y0T1VKclEyRldla0pMVkZWZloySm5JaXdpZVNJNkluVlVORmQ0Ym5sUllWcDNMVFI2VGxoQmRUTTBkbGxxU25aM1pGaHVaREo0V2pkeFZtVmhhbWQwYzBVaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsIm9yaWdpbiI6Ind3dy5nb29nbGUuY29tIn19fQ.xBd3Q-vka4bkaVBtUjLbimzh1HpzqxWxMaF8eD9l_A56N-81FmOXR-VuEdYmqnTE_TxRwFlDXtaWm_QOXgQXrw"
    ]
}"""
        val rpDid = "did:ion:EiB7J-k0f3ImG-zyFUFRmV4gaOsdfjy3kddJ_d9OOv7shA?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCZlBUWjFYbTR4TmM5NnZQcndvWTA1bkxfNDVZUG15b1Vnb3lSakFLQU1kZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUFFTk00cE5LYkZ6U2YxRk1YRE5sYTBVNlFCVG1rY1BHOE1wTGNRZFZPTktnIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQi1DTTU1SF92N0xaQW9CWEk2OXZEcnA1X2d0X2ptQzJublhCLVU5TlRxV2ciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnXzM3MzdmMjI2IiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJsck5PY18yb3RjM3ZUeEluZTBlckhUUy1GbGF4OUJrQ2FWekJLVFVfZ2JnIiwieSI6InVUNFd4bnlRYVp3LTR6TlhBdTM0dllqSnZ3ZFhuZDJ4WjdxVmVhamd0c0UifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val response = serializer.parse(DnsBindingResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linked_dids.first()
        runBlocking {
/*            val domainLinkageCredential = dnsBindingService.validateDomainLinkageCredentialJwt(domainLinkageCredentialJwt)
            val validated = dnsBindingService.verifyDidConfigResource(domainLinkageCredential, rpDid, "www.google.com")*/
            val validated = jwtDomainLinkageCredentialValidator.validate(domainLinkageCredentialJwt, rpDid, "www.google.com")
            assertThat(validated).isTrue()
        }
    }
}