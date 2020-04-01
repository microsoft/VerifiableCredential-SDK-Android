package com.microsoft.portableIdentity.sdk

import android.test.mock.MockContext
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import kotlinx.coroutines.runBlocking
import org.mockito.Mock
import org.mockito.Mockito.mock
import kotlin.test.Test

class CardManagerTest {

    @Test
    fun isGetRequestThrowing() {

        // val context: MockContext = mock(MockContext::class.java)
        val subtleCrypto = mock(AndroidSubtle::class.java)
        val keyStore = mock(AndroidKeyStore::class.java)

        val cryptoOperations = CryptoOperations(subtleCrypto, keyStore)

        runBlocking {
            // val request = PortableIdentitySdk.cardManager.getRequest("openid://?client_id=https%3a%2f%2fdidwebtest.azurewebsites.net%2fverify&response_type=id_token&scope=openid%20did_authn%20verify&nonce=gVOT2K3-IUG_4ciCgbyuGA&state=W2Ounn7tw02-nBXRpcjK8A&response_mode=form_post&request_uri=https%3a%2f%2fdidwebtest.azurewebsites.net%2foidc%2fxbox%2frequest%2f54e9e9ead34548a1a175705d881094b7")
            // print(request)
        }

    }
}