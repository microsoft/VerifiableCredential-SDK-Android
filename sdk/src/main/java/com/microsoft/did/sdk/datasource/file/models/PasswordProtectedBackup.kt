// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.BackupAndRestoreService
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import javax.crypto.spec.SecretKeySpec

const val PASSWORD_SET_SIZE = 12

class PasswordProtectedBackup internal constructor(
    token: JweToken,
    val password: List<String>? = null
    ) : JweProtectedBackup(token) {

    companion object {
        fun wrap(backup: UnprotectedBackup, serializer: Json): PasswordProtectedBackup {
            val data = backup.toString(serializer)
            val token = JweToken(data, JWEAlgorithm.PBES2_HS512_A256KW)
            token.contentType = backup.type
            return PasswordProtectedBackup(token, initializePasswordSet())
        }

        internal fun initializePasswordSet(setSize: Int = PASSWORD_SET_SIZE): List<String> {
            val random = SecureRandom()
            val wordSet = MutableList(setSize) { "" }
            val wordList = BackupAndRestoreService.getWordList()
            for (index in 0 until wordSet.count()) {
                var wordIndex: Int
                var word: String
                do {
                    wordIndex = random.nextInt(wordList.count())
                    word = wordList[wordIndex]
                } while (wordSet.contains(word))
                wordSet[index] = word
            }
            return wordSet
        }
    }

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

    suspend fun decrypt(password: String): Boolean {
        // this can be a very long operation, thus the suspend
        val words = password.split(Regex("\\s+")).filter { it.isNotBlank() }
        val secretKey = SecretKeySpec(
            words.joinToString(" ").toByteArray(),
            "RAW"
        )
        return (jweToken.decrypt(privateKey = secretKey) != null)
    }

    fun getDisplayPassword(): String {
        return password?.joinToString(" ") ?: ""
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