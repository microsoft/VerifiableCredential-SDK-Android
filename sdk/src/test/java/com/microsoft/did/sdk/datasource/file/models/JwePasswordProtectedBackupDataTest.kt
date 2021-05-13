// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.WalletMetadata
import com.microsoft.did.sdk.util.defaultTestSerializer
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.jwk.OctetSequenceKey
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class JwePasswordProtectedBackupDataTest {
    val password = "incredibly weak password, do not attempt."
    val payload = Microsoft2020UnprotectedBackupData(
        vcs = emptyMap(),
        vcsMetaInf = emptyMap(),
        metaInf = WalletMetadata(),
        identifiers = emptyList()
    )
    var passwordBackup: JwePasswordProtectedBackupData

    init {
        val token = JweToken(
            defaultTestSerializer.encodeToString(UnprotectedBackupData.serializer(), payload)
        )
        val secretKey = OctetSequenceKey.Builder(
            password.toByteArray()
        ).build()
        token.encrypt(
            secretKey, JWEHeader.Builder(
                JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256GCM
            ).build()
        )
        passwordBackup = JwePasswordProtectedBackupData(token)
    }

    @Test
    fun decryptTest() {
        assertFails {
            passwordBackup.decrypt("", defaultTestSerializer)
        }
        assertFails {
            passwordBackup.decrypt("not the password", defaultTestSerializer)
        }
        val actual = passwordBackup.decrypt(password, defaultTestSerializer)
        assertTrue(actual is Microsoft2020UnprotectedBackupData)
    }
}