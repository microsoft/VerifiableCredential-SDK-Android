// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.container.jwe

import com.microsoft.did.sdk.backup.container.ProtectionMethod
import com.microsoft.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.BadPasswordException
import com.microsoft.did.sdk.util.controlflow.FailedDecryptException
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.spec.SecretKeySpec

class JwePasswordProtectionMethod(
    val password: String
) : ProtectionMethod() {

    override fun wrap(unprotectedBackupData: UnprotectedBackupData, serializer: Json): ProtectedBackupData {
        val data = serializer.encodeToString(unprotectedBackupData)
        val token = JweToken(data)
        val headers = JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512)
            .contentType(unprotectedBackupData.type)
            .build()
        val secretKey = OctetSequenceKey.Builder(
            password.toByteArray()
        ).build()
        token.encrypt(secretKey, headers)
        return JwePasswordProtectedBackupData(token)
    }

    override fun unwrap(protectedBackupData: ProtectedBackupData, serializer: Json): UnprotectedBackupData {
        if (protectedBackupData !is JwePasswordProtectedBackupData) throw FailedDecryptException("Protection paramaters do not match backup contents")
        if (password.isEmpty()) throw BadPasswordException("Password can't be empty")

        val secretKey = SecretKeySpec(
            password.toByteArray(),
            "RAW"
        )
        val data = protectedBackupData.jweToken.decrypt(privateKey = secretKey)
        return serializer.decodeFromString(UnprotectedBackupData.serializer(), String(data))
    }
}