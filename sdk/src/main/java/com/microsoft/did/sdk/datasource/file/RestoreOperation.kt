package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.file.models.RestoreInteraction
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.security.KeyException
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


typealias RestoreVerifiableCredentialCallback = (verifiableCredential: VerifiableCredential, metadata: VCMetadata) -> Unit
typealias RestoreMetadataCallback = (metadata: WalletMetadata) -> Unit

class RestoreOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val serializer: Json
) {
    private lateinit var restoreVerifiedCredentialCallback: RestoreVerifiableCredentialCallback
    private lateinit var restoreMetadataCallback: RestoreMetadataCallback
    private lateinit var jweToken: JweToken
    fun initialize (restoreVerifiedCredentialCallback: RestoreVerifiableCredentialCallback, restoreMetadataCallback: RestoreMetadataCallback, backup: InputStream) {
        this.restoreVerifiedCredentialCallback = restoreVerifiedCredentialCallback
        this.restoreMetadataCallback = restoreMetadataCallback
        val jweString = String(backup.readBytes())

//        val stringBuilder = StringBuilder()
//        context.contentResolver.openInputStream(uri)?.use { stream ->
//            BufferedReader(InputStreamReader(stream)).useLines { lines ->
//                for (line in lines) {
//                    stringBuilder.append(line)
//                }
//            }
//        }
//        val content = stringBuilder.toString()
//        val token = JweToken.deserialize(content)

        this.jweToken = JweToken.deserialize(jweString)
        // validate we know this backup
        val cty = this.jweToken.getContentType()
        when (cty) {
            MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE ->
                return
            else ->
                throw AlgorithmException("Unknown backup file format: $cty")
        }
    }

    fun getRequiredUserInteraction(): RestoreInteraction {
        val alg = jweToken.getKeyAlgorithm()
        return if (alg.name.startsWith("PBE")) {
            RestoreInteraction.PASSWORD
        } else {
            RestoreInteraction.UNKNOWN
        }
    }

    suspend fun restoreWithPassword(password: String) {
        jweToken.decrypt(keyStore, SecretKeySpec(password.toByteArray(), "RAW"))
        restore()
    }

    private suspend fun restore() {
        if (this.restoreVerifiedCredentialCallback == null || this.restoreMetadataCallback == null) {
            throw AlgorithmException("Restore callback not set.")
        }
        val cty = this.jweToken.getContentType()
        when (cty) {
            MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE -> {
                val backup = serializer.decodeFromString<MicrosoftBackup2020>(jweToken.contentAsString)
                restoreFromMicrosoftBackup2020(backup)
            }
            else ->
                throw AlgorithmException("Unknown backup file format: $cty")
        }
    }


    private suspend fun restoreFromMicrosoftBackup2020(backup: MicrosoftBackup2020) {
        backup.identifiers.forEach { raw -> restoreIdentifier(raw) }
        backup.vcsToIterator(serializer).forEach {
                dataPair ->
            // TODO: Some validation that the VC is issued to an identifier under our control
            restoreVerifiedCredentialCallback(dataPair.first, dataPair.second)
        }
        restoreMetadataCallback(backup.metaInf)
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
