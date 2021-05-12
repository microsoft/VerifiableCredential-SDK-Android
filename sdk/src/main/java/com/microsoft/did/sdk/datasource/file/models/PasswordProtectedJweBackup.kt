// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.BadPasswordException
import kotlinx.serialization.json.Json
import javax.crypto.spec.SecretKeySpec

class PasswordProtectedJweBackup internal constructor(
    override val jweToken: JweToken,
) : JweProtectedBackup() {

    internal fun decrypt(password: String, serializer: Json): UnprotectedBackupData {
        if (password.isEmpty()) {
            throw BadPasswordException("Password can't be empty")
        }
        val secretKey = SecretKeySpec(
            password.toByteArray(),
            "RAW"
        )
        val data = jweToken.decrypt(privateKey = secretKey)
        return serializer.decodeFromString(UnprotectedBackupData.serializer(), String(data))
    }
}