// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.ProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedJweBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackupData
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethodException
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JweProtectedBackupFactory @Inject constructor(
    private val jsonSerializer: Json
) {
    fun parseBackup(jweString: String): ProtectedBackup {
        val token = JweToken.deserialize(jweString)
        val cty = token.contentType
        // for now we only know microsoft password, fail early.
        if (cty != Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE) {
            throw UnknownBackupFormatException("Backup of an unknown format: $cty")
        }
        val alg = token.getKeyAlgorithm()
        if (alg.name.startsWith("PBE")) {
            return PasswordProtectedJweBackup(token)
        } else {
            throw UnknownProtectionMethodException("Unknown backup protection method: $alg")
        }
    }

    fun createPasswordBackup(unprotectedBackupData: UnprotectedBackupData, password: String): PasswordProtectedJweBackup {
        val data = jsonSerializer.encodeToString(unprotectedBackupData)
        val token = JweToken(data)
        val headers = JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512)
            .contentType(unprotectedBackupData.type)
            .build()
        val secretKey = OctetSequenceKey.Builder(
            password.toByteArray()
        ).build()
        token.encrypt(secretKey, headers)
        return PasswordProtectedJweBackup(token)
    }
}