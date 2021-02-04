package com.microsoft.did.sdk.datasource.file

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyException
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.random.Random

class RestoreOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val serializer: Json
) {

    private fun parseBackupfileFromUri(context: Context, uri: Uri, password: String): MicrosoftBackup2020 {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).useLines { lines ->
                for (line in lines) {
                    stringBuilder.append(line)
                }
            }
        }
        val content = stringBuilder.toString()
        val token = JweToken.deserialize(content)
        val secretKey = SecretKeySpec(password.toByteArray(), "RAW")
        // transform password to a decryption key

        token.decrypt(keyStore, secretKey)

        return serializer.decodeFromString<MicrosoftBackup2020>("token.contentAsString")
    }

    private suspend fun restoreIdentifier (
        identifierData: RawIdentity
    ): Identifier {
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
        var recoveryKeyRef: String = ""
        var updateKeyRef: String = ""
        for (key in identifierData.keys) {
            if (signingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.SIGN, KeyOperation.VERIFY).contains(it) } == true ||
                 key.keyUse == KeyUse.SIGNATURE)) {
                signingKeyRef = importKey(key)
            }
            if (encryptingKeyRef.isBlank() &&
                (key.keyOperations?.containsAll(listOf(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)) == true ||
                 key.keyOperations?.containsAll(listOf(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)) == true ||
                 key.keyUse == KeyUse.ENCRYPTION)) {
                encryptingKeyRef = importKey(key)
            }
        }
        if (updateKeyRef.isEmpty() || recoveryKeyRef.isBlank()) {
            throw KeyException("update and recovery key required")
        }
        val id = Identifier(
            identifierData.id,
            signingKeyRef,
            encryptingKeyRef,
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        identifierRepository.insert(id)
        return id
    }

    private fun importKey(
        jwk: JWK
    ): String {
        val kid = jwk.keyID ?: throw com.microsoft.did.sdk.util.controlflow.KeyException("Imported JWK has no key id.")
        if (!keyStore.containsKey(kid)) {
            keyStore.storeKey(jwk, kid)
        }
        return kid
    }
}
