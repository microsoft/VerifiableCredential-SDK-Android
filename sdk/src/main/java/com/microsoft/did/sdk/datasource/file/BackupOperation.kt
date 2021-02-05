package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.BackupSecurityMethod
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.security.SecureRandom
import javax.inject.Inject

const val PASSWORD_SET_SIZE = 12

class BackupOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val serializer: Json
) {
    private lateinit var method: BackupSecurityMethod
    private var jweToken: JweToken? = null

    private val wordList: List<String> = emptyList()
    private val passwordSet: List<String> = initializePasswordSet(PASSWORD_SET_SIZE)

    fun initialize(lifecycleContext: CoroutineScope,
                   metadata: WalletMetadata,
                   verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>,
                   method: BackupSecurityMethod) {
        this.method = method
        lifecycleContext.launch(Dispatchers.IO) {
            createBackup(metadata, verifiableCredentials, MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE)
        }
    }

    fun getWordList(): List<String> {
        return wordList
    }

    fun getDisplayPassword(): String {
        return passwordSet.joinToString(" ")
    }

    fun confirmPassword(password: String): Boolean {
        if (method != BackupSecurityMethod.PASSWORD) {
            throw RuntimeException("Backup was not created with method PASSWORD")
        }
        val words = password.split(Regex("\\s+"))
        for (i in 0..passwordSet.count()) {
            if (!words[i].equals(passwordSet[i], true)) {
                return false
            }
        }
        return true
    }

    fun writeOutput(output: OutputStream) {
        jweToken?.let {
            token ->
            output.write(token.serialize().toByteArray())
            output.close()
            return
        }
        throw RuntimeException("Backup encryption has not completed yet.")
    }

    private fun initializePasswordSet(setSize: Int): List<String> {
        val random = SecureRandom()
        val wordSet = MutableList(setSize) { "" }
        for (index in 0..wordSet.count()) {
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

    private fun createJWEUsingPassword(backupData: String) {
        jweToken = JweToken(plaintext = backupData, algorithm = JWEAlgorithm.PBES2_HS512_A256KW)
        jweToken!!.encrypt(OctetSequenceKey.Builder(getDisplayPassword().toByteArray()).build())
    }

    private suspend fun createBackup(metadata: WalletMetadata,
                                     verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>,
                                     type: String = MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE) {
        val backup = when (type) {
            MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE -> {
                createMicrosoftBackup(metadata, verifiableCredentials)
            }
            else -> {
                throw AlgorithmException("Unknown backup type $type")
            }
        }

        val payload = serializer.encodeToString(backup)
        when (method) {
            BackupSecurityMethod.PASSWORD -> {
                createJWEUsingPassword(payload)
            }
            BackupSecurityMethod.UNKNOWN ->
                throw IllegalArgumentException("BackupOperation method cannot be UNKNOWN")
            BackupSecurityMethod.NONE ->
                throw IllegalArgumentException("BackupOperation method NONE not allowed")
        }
    }

    private suspend fun createMicrosoftBackup(metadata: WalletMetadata,
                                              verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>): MicrosoftBackup2020 {

        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VCMetadata>()
        val ownedDids = mutableListOf<String>()

        verifiableCredentials.forEach { vcPair ->
            val jti = vcPair.first.jti
            vcMap[jti] = vcPair.first.raw
            vcMetaMap[jti] = vcPair.second
            ownedDids.add(vcPair.first.contents.sub)
        }

        val identifiers = ownedDids.map { identifierRepository.queryByIdentifier(it)?.let {
            identity ->
            val keys = listOf(
                identity.encryptionKeyReference,
                identity.signatureKeyReference,
                identity.updateKeyReference,
                identity.recoveryKeyReference
            ).mapNotNull { keyId ->
                if (keyId.isNotBlank()) {
                    keyStore.getKey(keyId)
                } else {
                    null
                }
            }
            RawIdentity(
                identity.id,
                name = identity.name,
                keys
            )
        }}.filterNotNull()

        return MicrosoftBackup2020(
            metaInf = metadata,
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            identifiers = identifiers
        )
    }
}