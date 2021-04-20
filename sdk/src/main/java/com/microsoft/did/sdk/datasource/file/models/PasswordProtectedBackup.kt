// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.BadPassword
import com.microsoft.did.sdk.util.controlflow.FailedDecrypt
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.json.Json
import javax.crypto.spec.SecretKeySpec

class PasswordProtectedBackup internal constructor(
    override val jweToken: JweToken,
) : JweProtectedBackup() {

    internal fun decrypt(password: String, serializer: Json): UnprotectedBackup {
        // empty passwords throw if attempted
        if (password.isEmpty()) {
            throw BadPassword("Failed to decrypt")
        }
        val secretKey = SecretKeySpec(
            password.toByteArray(),
            "RAW"
        )
        val data = jweToken.decrypt(privateKey = secretKey);
        return serializer.decodeFromString(UnprotectedBackup.serializer(), String(data))
    }
}