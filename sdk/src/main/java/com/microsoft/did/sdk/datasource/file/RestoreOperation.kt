package com.microsoft.did.sdk.datasource.file

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyException
import javax.inject.Inject
import kotlin.random.Random

class RestoreOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore
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
        val secretKey = OctetSequenceKey.Builder(password.toByteArray())
            .build()
        // transform password to a decryption key

        token.decrypt(keyStore, secretKey)

        return serializer.decodeFromString<MicrosoftBackup2020>("token.contentAsString")
    }

    private suspend fun restoreIdentifier (
        identifierData: RawIdentity
    ): Identifier {
        val alias = Base64.encodeToString(Random.nextBytes(2), Constants.BASE64_URL_SAFE)
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
        var recoveryKeyRef: String = ""
        var updateKeyRef: String = ""
        for (key in identifierData.keys) {
            if (key.key_ops?.containsAll(listOf("sign", "verify")) == true) {
                signingKeyRef = importKey(key, alias)
            }
            if (key.key_ops?.containsAll(listOf("encrypt", "decrypt")) == true ||
                key.key_ops?.containsAll(listOf("wrapKey", "unwrapKey")) == true) {
                encryptingKeyRef = importKey(key, alias)
            }
            if (key.key_ops?.contains("update") == true) {
                updateKeyRef = importKey(key, alias)
            }
            if (key.key_ops?.contains("recover") == true) {
                recoveryKeyRef = importKey(key, alias)
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
        jwk: JsonWebKey,
        alias: String
    ): String {
//        val key = when (jwk.kty) {
//            KeyType.RSA.value -> {
//                RsaPrivateKey(jwk)
//            }
//            KeyType.EllipticCurve.value -> {
//                EllipticCurvePrivateKey(jwk)
//            }
//            else -> {
//                throw KeyException("Unsupported key type ${jwk.kty}")
//            }
//        }
//        val knownKeys = cryptoOperations.keyStore.list().entries.filter {
//            it.value.kids.contains(key.kid)
//        }
//        if (knownKeys.isNotEmpty()) {
//            return knownKeys.first().key
//        }
//
//        val keyRef = "${alias}_${if (key.kid.isNullOrEmpty())
//            {
//                Base64Url.encode(Random.nextBytes(2))
//            } else{
//                key.kid
//            }}"
//        cryptoOperations.keyStore.save(keyRef, key);
//        return keyRef;
        return ""
    }
}
