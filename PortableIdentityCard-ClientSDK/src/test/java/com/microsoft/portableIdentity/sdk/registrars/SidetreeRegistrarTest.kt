package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.PortableIdentitySdk
import com.microsoft.portableIdentity.sdk.utilities.Constants
import org.assertj.core.api.Assertions
import org.junit.Test

class SidetreeRegistrarTest {
    private val registrar: SidetreeRegistrar = SidetreeRegistrar("http://10.91.6.163:3000")
/*
    @Test
    suspend fun createIdentifierTest() {
        val identifier =  registrar.register(Constants.SIGNATURE_KEYREFERENCE, Constants.RECOVERY_KEYREFERENCE, cryptoOperations)
        Assertions.assertThat(PortableIdentitySdk.identifierManager.did).isNotNull
    }*/
}