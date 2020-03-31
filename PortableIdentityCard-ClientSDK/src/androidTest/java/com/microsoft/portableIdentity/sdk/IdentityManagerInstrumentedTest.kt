package com.microsoft.portableIdentity.sdk

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import org.junit.Test
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat

@RunWith(AndroidJUnit4ClassRunner::class)
class IdentityManagerInstrumentedTest {
    private val logger: ILogger
    private val signatureKeyReference: String
    private val encryptionKeyReference: String
    private val recoveryKeyReference: String
    private val registrationUrl: String
    private val resolverUrl: String
    private val identityManager: IdentityManager

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        logger = ConsoleLogger()
        registrationUrl = "http://10.91.6.163:3000"
        resolverUrl = "http://10.91.6.163:3000"
        signatureKeyReference = "signature"
        encryptionKeyReference = "encryption"
        recoveryKeyReference = "recovery"
        val didSdkConfig = DidSdkConfig(context, logger, signatureKeyReference, encryptionKeyReference, recoveryKeyReference, registrationUrl, resolverUrl)
        identityManager = IdentityManager(didSdkConfig)
    }

    @Test
    fun createIdentifierTest() {
        assertThat(identityManager.did).isNotNull
    }
}