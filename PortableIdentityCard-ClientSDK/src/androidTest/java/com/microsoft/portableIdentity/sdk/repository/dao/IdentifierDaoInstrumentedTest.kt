// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.dao

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.SdkDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class IdentifierDaoInstrumentedTest {
    var identifierDao: IdentifierDao
    var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        identifierDao = sdkDatabase.identifierDao()
    }

    @Test
    fun insertAndRetrieveIdentifierByIdTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier)
        val actualIdentifier = identifierDao.queryByIdentifier(suppliedIdentifier.id)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier)
    }

    @Test
    fun insertAndRetrieveIdentifierByNameTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier)
        val actualIdentifierName = "testIdentifierName"
        val actualIdentifier = identifierDao.queryByName(actualIdentifierName)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier)
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }
}