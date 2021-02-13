// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.IoFailure
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.Result.Success
import com.microsoft.did.sdk.util.controlflow.Result.Failure
import java.io.IOException

abstract class JweProtectedBackup internal constructor (
    val jweToken: JweToken
    ) {

    companion object {
        suspend fun parseBackup(backupFile: InputStream, serializer: Json): Result<JweProtectedBackup> {
            val jweString = String(backupFile.readBytes())
            val token = JweToken.deserialize(jweString)
            val cty = token.contentType
            // for now we only know microsoft password, fail early.
            if (cty != MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE) {
                return Failure(UnknownBackupFormat("Backup of an unknown format: $cty"))
            }
            val alg = token.getKeyAlgorithm()
            return if (alg.name.startsWith("PBE")) {
                Success(PasswordProtectedBackup(token, serializer))
            } else {
                Failure(UnknownProtectionMethod("Unknown backup protection method: $alg"))
            }
        }
    }

    abstract suspend fun encrypt()

    fun getBackupType(): String? {
        return jweToken.contentType
    }

    fun getBackup(jsonSerializer: Json): UnprotectedBackup {
        return jsonSerializer.decodeFromString(UnprotectedBackup.serializer(), jweToken.contentAsString)
    }

    fun writeOutput(output: OutputStream): Result<Unit> {
        return try {
            output.write(jweToken.serialize().toByteArray())
            output.flush()
            output.close()
            Result.Success(payload = Unit)
        } catch (exception: IOException) {
            Result.Failure(IoFailure("Failed to write backup", exception))
        }
    }
}