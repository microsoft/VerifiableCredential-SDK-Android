// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethodException
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JweProtectedBackupFactory @Inject constructor(
    private val jsonSerializer: Json
) {
    fun parseBackup(backupFile: InputStream): JweProtectedBackup {
        val jweString = String(backupFile.readBytes())
        val token = JweToken.deserialize(jweString)
        val cty = token.contentType
        // for now we only know microsoft password, fail early.
        if (cty != MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE) {
            throw UnknownBackupFormatException("Backup of an unknown format: $cty")
        }
        val alg = token.getKeyAlgorithm()
        return if (alg.name.startsWith("PBE")) {
            return PasswordProtectedBackup(token)
        } else {
            throw UnknownProtectionMethodException("Unknown backup protection method: $alg")
        }
    }

    fun createPasswordBackup(unprotectedBackup: UnprotectedBackup, password: String): PasswordProtectedBackup {
        val data = jsonSerializer.encodeToString(unprotectedBackup)
        val token = JweToken(data)
        val headers = JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512)
            .contentType(unprotectedBackup.type)
            .build()
        val secretKey = OctetSequenceKey.Builder(
            password.toByteArray()
        ).build()
        token.encrypt(secretKey, headers)
        return PasswordProtectedBackup(token)
    }
}