// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.IoFailure
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.OutputStream

abstract class JweProtectedBackup {
    abstract val jweToken: JweToken

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