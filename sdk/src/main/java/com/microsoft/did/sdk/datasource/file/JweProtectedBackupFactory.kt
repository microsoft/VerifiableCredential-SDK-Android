// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.JweProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.MicrosoftUnprotectedBackup2020
import com.microsoft.did.sdk.datasource.file.models.PasswordProtectedBackup
import com.microsoft.did.sdk.datasource.file.models.UnprotectedBackup
import com.microsoft.did.sdk.util.controlflow.IoFailure
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnknownBackupFormat
import com.microsoft.did.sdk.util.controlflow.UnknownProtectionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
            throw UnknownBackupFormat("Backup of an unknown format: $cty")
        }
        val alg = token.getKeyAlgorithm()
        return if (alg.name.startsWith("PBE")) {
            return PasswordProtectedBackup(token)
        } else {
            throw UnknownProtectionMethod("Unknown backup protection method: $alg")
        }
    }

    fun createPasswordBackup(unprotectedBackup: UnprotectedBackup, password: String): PasswordProtectedBackup {
        val data = jsonSerializer.encodeToString(unprotectedBackup)
        val token = JweToken(data, JWEAlgorithm.PBES2_HS512_A256KW)
        token.contentType = unprotectedBackup.type
        val secretKey = OctetSequenceKey.Builder(
            password.toByteArray()
        ).build()
        token.encrypt(secretKey)
        return PasswordProtectedBackup(token)
    }



    internal fun writeOutput(backup: JweProtectedBackup, output: OutputStream) {
            output.write(backup.jweToken.serialize().toByteArray())
            output.flush()
            output.close()
    }
}