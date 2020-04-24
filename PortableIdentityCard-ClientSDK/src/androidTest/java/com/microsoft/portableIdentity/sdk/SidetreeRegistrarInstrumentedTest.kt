// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.registrars.SidetreeRegistrar
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
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
        val serializer = Serializer()
        val keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ecSubtle = EllipticCurveSubtleCrypto(androidSubtle, serializer)
        registrar = SidetreeRegistrar("http://10.91.6.163:3000", serializer, PortableIdentitySdk.identifierManager.identifierRepository)
        resolver = Resolver("http://10.91.6.163:3000", PortableIdentitySdk.identifierManager.identifierRepository)
        cryptoOperations = CryptoOperations(androidSubtle, keyStore)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

    @Test
    fun idCreationTest() {
        runBlocking {
            val id = registrar.register(
                "sign",
                "recover",
                cryptoOperations
            )
            if(id is Result.Success)
                assertThat(id.payload).isNotNull()
        }
    }

    @Test
    fun createAndSaveIdTest() {
        runBlocking {
            val identifier = PortableIdentitySdk.identifierManager.getIdentifier()
        }
    }

    @Test
    fun resolverTest() {
        val id = "did:ion:test:EiDRPAETJeLINVPwpSqboRYmKyWh8JbM2FVHmaPdLEw6ng?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEa2d5eHRxNkZTVUNyV19MM2Jzby05X3hIazlETUI2TU1ROUI0U25qTVlPQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJBYUt1a3BDMFhBeW9qRUtpVVdXZS1KeUxYbzBIZzExY3FFNzc0T2xRVjZZIiwieSI6Imt5dXViNDBXaTFoMENMVWFQZ3h5QXNFd1hpWlFLX2RXUXVzYjhZTllzMW8ifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpQ3FBajBUanFoaU5LU1hWNllfTThzdFZFODNHVmltLXNwMHhzVERBTERaTlEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQlZMOEVDZ2ViaTUwSVE3SzVETzRxOUIxaGd0akxGWjhnWEJ0RWpiVjFxV0EiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWduaW5nS2V5IiwidHlwZSI6IlNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiY0Y0ZFFOXzUxZGxOOXkzdml1REJWM2hOcm9zbHpha2dKYW9OZzJTcFNVYyIsInkiOiJVdWdtcVljZHJyWlBZdVZ1d0J6dENHRUtEYlZhbzE0OFd4ZjM5U0pOVTRRIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XSwic2VydmljZUVuZHBvaW50cyI6W3siaWQiOiJzZXJ2aWNlRW5kcG9pbnRJZDEyMyIsInR5cGUiOiJzb21lVHlwZSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vd3d3LnVybC5jb20ifV19fV19"
        runBlocking {
            val result = resolver.resolve(id)
            if(result is Result.Success)
                assertThat(result.payload).isNotNull()
        }
    }

    @Test
    fun deserializeTest() {
        val doc = """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:test:EiDRPAETJeLINVPwpSqboRYmKyWh8JbM2FVHmaPdLEw6ng?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEa2d5eHRxNkZTVUNyV19MM2Jzby05X3hIazlETUI2TU1ROUI0U25qTVlPQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJBYUt1a3BDMFhBeW9qRUtpVVdXZS1KeUxYbzBIZzExY3FFNzc0T2xRVjZZIiwieSI6Imt5dXViNDBXaTFoMENMVWFQZ3h5QXNFd1hpWlFLX2RXUXVzYjhZTllzMW8ifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpQ3FBajBUanFoaU5LU1hWNllfTThzdFZFODNHVmltLXNwMHhzVERBTERaTlEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQlZMOEVDZ2ViaTUwSVE3SzVETzRxOUIxaGd0akxGWjhnWEJ0RWpiVjFxV0EiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWduaW5nS2V5IiwidHlwZSI6IlNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiY0Y0ZFFOXzUxZGxOOXkzdml1REJWM2hOcm9zbHpha2dKYW9OZzJTcFNVYyIsInkiOiJVdWdtcVljZHJyWlBZdVZ1d0J6dENHRUtEYlZhbzE0OFd4ZjM5U0pOVTRRIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XSwic2VydmljZUVuZHBvaW50cyI6W3siaWQiOiJzZXJ2aWNlRW5kcG9pbnRJZDEyMyIsInR5cGUiOiJzb21lVHlwZSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vd3d3LnVybC5jb20ifV19fV19","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:test:EiDRPAETJeLINVPwpSqboRYmKyWh8JbM2FVHmaPdLEw6ng?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEa2d5eHRxNkZTVUNyV19MM2Jzby05X3hIazlETUI2TU1ROUI0U25qTVlPQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJBYUt1a3BDMFhBeW9qRUtpVVdXZS1KeUxYbzBIZzExY3FFNzc0T2xRVjZZIiwieSI6Imt5dXViNDBXaTFoMENMVWFQZ3h5QXNFd1hpWlFLX2RXUXVzYjhZTllzMW8ifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpQ3FBajBUanFoaU5LU1hWNllfTThzdFZFODNHVmltLXNwMHhzVERBTERaTlEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQlZMOEVDZ2ViaTUwSVE3SzVETzRxOUIxaGd0akxGWjhnWEJ0RWpiVjFxV0EiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWduaW5nS2V5IiwidHlwZSI6IlNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiY0Y0ZFFOXzUxZGxOOXkzdml1REJWM2hOcm9zbHpha2dKYW9OZzJTcFNVYyIsInkiOiJVdWdtcVljZHJyWlBZdVZ1d0J6dENHRUtEYlZhbzE0OFd4ZjM5U0pOVTRRIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XSwic2VydmljZUVuZHBvaW50cyI6W3siaWQiOiJzZXJ2aWNlRW5kcG9pbnRJZDEyMyIsInR5cGUiOiJzb21lVHlwZSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vd3d3LnVybC5jb20ifV19fV19"}],"service":[{"id":"#serviceEndpointId123","type":"someType","serviceEndpoint":"https://www.url.com"}],"publicKey":[{"id":"#signingKey","controller":"","type":"Secp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"cF4dQN_51dlN9y3viuDBV3hNroslzakgJaoNg2SpSUc","y":"UugmqYcdrrZPYuVuwBztCGEKDbVao148Wxf39SJNU4Q"}}],"authentication":["#signingKey"]},"methodMetadata":{"operationPublicKeys":[{"id":"#signingKey","controller":"","type":"Secp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"cF4dQN_51dlN9y3viuDBV3hNroslzakgJaoNg2SpSUc","y":"UugmqYcdrrZPYuVuwBztCGEKDbVao148Wxf39SJNU4Q"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"AaKukpC0XAyojEKiUWWe-JyLXo0Hg11cqE774OlQV6Y","y":"kyuub40Wi1h0CLUaPgxyAsEwXiZQK_dWQusb8YNYs1o"}}}"""
        val idDoc = Serializer().parse(IdentifierResponse.serializer(), doc)
        assertThat(idDoc.didDocument.id).isNotNull()
    }

    @Test
    fun signAndVerifyTest() {
        val serializer = Serializer()
        val test = "test string"
        val testPayload = test.toByteArray()
        var signKey = ""
        runBlocking {
            signKey =
            when(val id = PortableIdentitySdk.identifierManager.getIdentifier()) {
                is Result.Success -> id.payload.signatureKeyReference
                else -> ""
            }
        }

        val token = JwsToken(testPayload, serializer)
        token.sign(signKey, cryptoOperations)
        assertThat(token.signatures).isNotNull
        val matched = token.verify(cryptoOperations)
        assertThat(matched).isTrue()
    }

}