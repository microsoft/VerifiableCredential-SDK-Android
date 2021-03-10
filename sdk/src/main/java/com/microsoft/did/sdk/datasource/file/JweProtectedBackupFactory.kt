// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
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
}