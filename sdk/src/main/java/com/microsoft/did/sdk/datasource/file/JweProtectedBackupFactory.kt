// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject

class JweProtectedBackupFactory @Inject constructor(
    private val jsonSerializer: Json
) {
    suspend fun parseBackup(backupFile: InputStream): Result<JweProtectedBackup> {
        val jweString = String(backupFile.readBytes())
        val token = JweToken.deserialize(jweString)
        val cty = token.contentType
        // for now we only know microsoft password, fail early.
        if (cty != MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE) {
            return Result.Failure(UnknownBackupFormat("Backup of an unknown format: $cty"))
        }
        val alg = token.getKeyAlgorithm()
        return if (alg.name.startsWith("PBE")) {
            Result.Success(PasswordProtectedBackup(token, jsonSerializer))
        } else {
            Result.Failure(UnknownProtectionMethod("Unknown backup protection method: $alg"))
        }
    }

    suspend fun createPasswordBackup(unprotectedBackup: UnprotectedBackup, password: String): Result<PasswordProtectedBackup> {
        val data = unprotectedBackup.toString(jsonSerializer)
        val token = JweToken(data, JWEAlgorithm.PBES2_HS512_A256KW)
        token.contentType = unprotectedBackup.type
        val words = password.split(Regex("\\s+")).filter { it.isNotBlank() }
        val secretKey = OctetSequenceKey.Builder(
            words.joinToString(" ").toByteArray()
        ).build()
        token.encrypt(secretKey)
        return Result.Success(PasswordProtectedBackup(token, jsonSerializer))
    }
}