// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.BadPassword
import com.microsoft.did.sdk.util.controlflow.FailedDecrypt
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.json.Json
import java.lang.Exception
import javax.crypto.spec.SecretKeySpec

const val PASSWORD_SET_SIZE = 12

class PasswordProtectedBackup internal constructor(
    token: JweToken,
    val serializer: Json,
    val password: List<String>? = null
    ) : JweProtectedBackup(token) {

    internal constructor(
        backup: UnprotectedBackup,
        password: String,
        serializer: Json
    ) : this({ unprotectedBackup: UnprotectedBackup ->
            val data = unprotectedBackup.toString(serializer)
            val token = JweToken(data, JWEAlgorithm.PBES2_HS512_A256KW)
            token.contentType = unprotectedBackup.type
            token
        }(backup),
        serializer,
        password.split(Regex("\\s+")).filter{ it.isNotBlank() })

    override suspend fun encrypt() {
        // this can be a very long operation, thus the suspend
        this.password?.let { password ->
            val secretKey = OctetSequenceKey.Builder(
                password.joinToString(" ").toByteArray()
            ).build()
            jweToken.encrypt(secretKey)
            return
        }
        throw RuntimeException("No password has been initialized")
    }

    suspend fun decrypt(password: String): Result<UnprotectedBackup?> {
        // this can be a very long operation, thus the suspend
        val words = password.split(Regex("\\s+")).filter { it.isNotBlank() }
        try {
            val secretKey = SecretKeySpec(
                words.joinToString(" ").toByteArray(),
                "RAW"
            )
            jweToken.decrypt(privateKey = secretKey)?.let {
                data ->
                return Result.Success(payload = serializer.decodeFromString(UnprotectedBackup.serializer(), String(data)))
            }
            return Result.Failure(BadPassword("Failed to decrypt"))
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(FailedDecrypt("Unknown error", exception))
        }
    }

    fun confirmPassword(password: String): Boolean {
        if (this.password != null) {
            val words = password.split(Regex("\\s+")).filter{ it.isNotBlank() }
            if (words.count() != this.password.count()) {
                return false
            }
            for (i in 0 until this.password.count()) {
                if (!words[i].equals(this.password[i], true)) {
                    return false
                }
            }
            return true
        }
        return false
    }
}