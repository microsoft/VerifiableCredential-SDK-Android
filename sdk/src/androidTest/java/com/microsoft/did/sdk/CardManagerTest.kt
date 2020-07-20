// Copyright (c) Microsoft Corporation. All rights reserved

/*
package com.microsoft.did.sdk

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test

class CardManagerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var vchRepo: VerifiableCredentialHolderRepository
    private var cardManager: VerifiableCredentialManager
    private var identifierManager: IdentifierManager

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        VerifiableCredentialSdk.init(context)
        cardManager = VerifiableCredentialSdk.verifiableCredentialManager
        vchRepo = cardManager.vchRepository
        identifierManager = VerifiableCredentialSdk.identifierManager
    }

    @Test
    fun revokeTest() {
        runBlocking {
            val verifiableCredentials = cardManager.getVchById("urn:pic:9dd66428-ebee-4d8a-a1ba-f506875fbf9d")
            val mock: VerifiableCredentialHolder = mockk()
            val verifiableCredential = verifiableCredentials.getOrAwaitValue() ?: mock
//            val id = identifierManager.getMasterIdentifier()
//            val displayContract: DisplayContract = mockk()
//            val vch = VerifiableCredentialHolder("", verifiableCredential, (id as Result.Success).payload, displayContract)
            val status = cardManager.revokeVerifiablePresentation(verifiableCredential, null, null)
            Assertions.assertThat(status).isInstanceOf(Result.Success::class.java)
            Assertions.assertThat((status as Result.Success).payload.toLowerCase()).isEqualTo("revoked")
        }
    }
}*/
