// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.UnSupportedAlgorithmException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

abstract class JweProtectedBackup internal constructor (
    val jweToken: JweToken
    ) {
    companion object {
        suspend fun parseBackup(backupFile: InputStream): JweProtectedBackup {
            val jweString = String(backupFile.readBytes())
            val token = JweToken.deserialize(jweString)
            val cty = token.contentType
            // for now we only know microsoft password, fail early.
            if (cty != MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE) {
                throw IllegalArgumentException("Backup of an unknown format: $cty")
            }
            val alg = token.getKeyAlgorithm()
            if (alg.name.startsWith("PBE")) {
                return PasswordProtectedBackup(token)
            } else {
                throw UnSupportedAlgorithmException("Unknown backup protection method")
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

    fun writeOutput(output: OutputStream) {
        output.write(jweToken.serialize().toByteArray())
        output.flush()
        output.close()
    }
}