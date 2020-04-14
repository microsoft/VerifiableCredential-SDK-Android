/*
package com.microsoft.portableIdentity.sdk.registrars

import android.content.Context
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import com.microsoft.portableIdentity.sdk.utilities.Constants
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import javax.inject.Inject

abstract class SidetreeRegistrarTest {

    @MockK
    abstract val context: Context
    @InjectMockKs
    private val keyStore = AndroidKeyStore(context)
    @MockK
    private val subtle: SubtleCrypto = AndroidSubtle(keyStore)
    private val registrar: SidetreeRegistrar = SidetreeRegistrar("http://10.91.6.163:3000")
    @InjectMockKs
    private val cryptoOperations: CryptoOperations
    private val registrar: SidetreeRegistrar = SidetreeRegistrar("http://10.91.6.163:3000")
    private val keyStore: KeyStore

//    init {
        keyStore = InMemoryKeyStore()
//        subtle = Subtle(setOf(MockProvider(W3cCryptoApiConstants.Secp256k1.value)))

        cryptoOperations = CryptoOperations(subtle, keyStore)
        keyStore = AndroidKeyStore(context)
        subtle = AndroidSubtle(keyStore)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(subtle, SubtleCryptoScope.All)
        )
//    }

    @Test
    fun createIdentifierTest() {
        runBlocking {
            val identifierDocument = registrar.register(Constants.SIGNATURE_KEYREFERENCE, Constants.RECOVERY_KEYREFERENCE, cryptoOperations)
            assertThat(identifierDocument).isNotNull
        }
    }
}*/
