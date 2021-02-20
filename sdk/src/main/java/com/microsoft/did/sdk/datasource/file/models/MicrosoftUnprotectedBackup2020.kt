package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.MalformedIdentity
import com.microsoft.did.sdk.util.controlflow.MalformedMetadata
import com.microsoft.did.sdk.util.controlflow.MalformedVerifiableCredential
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * @constructor
 * @param vcs map of VC identifier (JTI) to raw VC token data
 * @param vcsMetaInf map of VC Identifier (JTI) to VC metadata
 * @param metaInf backup metadata
 * @param identifiers List of raw identifiers.
 */
@Serializable
@SerialName(MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE)
class MicrosoftUnprotectedBackup2020 (
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, VCMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>,
) : UnprotectedBackup() {
    override val type: String
        get() = MICROSOFT_BACKUP_TYPE

    companion object {
        const val MICROSOFT_BACKUP_TYPE = "MicrosoftWallet2020"
    }

    suspend fun import(
        walletMetadataCallback: suspend (WalletMetadata) -> Unit,
        verifiableCredentialCallback: suspend (VerifiableCredential, VCMetadata) -> Unit,
        listVerifiableCredentialCallback: suspend () -> List<String>,
        deleteVerifiableCredentialCallback: suspend (String) -> Unit,
        identityRepository: IdentifierRepository,
        keyStore: EncryptedKeyStore,
        jsonSerializer: Json): Result<Unit> {

        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()
        try {
            this.identifiers.forEach { raw ->
                val pair = parseRawIdentifier(raw)
                identifiers.add(pair.first)
                keySet = keySet.union(pair.second)
            }
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(MalformedIdentity("unhandled exception thrown", exception))
        }

        val existingVCs = listVerifiableCredentialCallback().toSet()
        val vcsToRemove = existingVCs.subtract(this.vcs.keys)
        val vcsNotToBeRemoved = existingVCs.intersect(this.vcs.keys)

        val identifiersToRemove = identityRepository.queryAllLocal().filter {
                dbIdentifier ->
            identifiers.all { identifier -> identifier.id != dbIdentifier.id } }

        val vcsAdded = mutableListOf<String>()
        try {
            this.vcsToIterator(jsonSerializer).forEach {
                dataPair ->
                vcsAdded.add(dataPair.first.jti)
                // TODO: Some validation that the VC is issued to an identifier under our control
                verifiableCredentialCallback(dataPair.first, dataPair.second)
            }
            keySet.forEach { key -> importKey(key, keyStore) }
            identifiers.forEach { id -> identityRepository.insert(id) }
            vcsToRemove.forEach { vcId -> deleteVerifiableCredentialCallback(vcId) }
            identifiersToRemove.forEach { identifier -> identityRepository.deleteIdentifier(identifier.id) }

        } catch (exception: SdkException) {
            vcsAdded.forEach { if (!vcsNotToBeRemoved.contains(it)) { deleteVerifiableCredentialCallback(it) } }
            return Result.Failure(exception)
        } catch (exception: Exception) {
            vcsAdded.forEach { if (!vcsNotToBeRemoved.contains(it)) { deleteVerifiableCredentialCallback(it) } }
            return Result.Failure(MalformedVerifiableCredential("unhandled exception thrown", exception))
        }

        try {
            walletMetadataCallback(this.metaInf)
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(MalformedMetadata("unhandled exception thrown", exception))
        }
        return Result.Success(Unit)
    }

    private fun parseRawIdentifier (
        identifierData: RawIdentity
    ): Pair<Identifier, Set<JWK>> {
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
        val recoveryKeyRef: String = identifierData.recoveryKey
        val updateKeyRef: String = identifierData.updateKey
        val keySet = mutableSetOf<JWK>()
        for (key in identifierData.keys) {
            if (signingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.SIGN, KeyOperation.VERIFY).contains(it) } == true ||
                    key.keyUse == KeyUse.SIGNATURE)) {
                signingKeyRef = getKidFromJWK(key)
                keySet.add(key)
            } else if (encryptingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.ENCRYPT, KeyOperation.DECRYPT).contains(it) } == true ||
                    key.keyOperations?.any { listOf(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY).contains(it) } == true ||
                    key.keyUse == KeyUse.ENCRYPTION)) {
                encryptingKeyRef = getKidFromJWK(key)
                keySet.add(key)
            } else if (key.keyID == updateKeyRef) {
                getKidFromJWK(key)
                keySet.add(key)
            } else if (key.keyID == recoveryKeyRef) {
                getKidFromJWK(key)
                keySet.add(key)
            }
        }
        if (updateKeyRef.isBlank() || recoveryKeyRef.isBlank()) {
            throw MalformedIdentity("update and recovery key required")
        }
        val id = Identifier(
            identifierData.id,
            signingKeyRef,
            encryptingKeyRef,
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        return Pair(id, keySet)
    }

    private fun getKidFromJWK(jwk: JWK): String {
        return jwk.keyID ?: throw KeyException("Imported JWK has no key id.")
    }

    private fun importKey(
        jwk: JWK,
        keyStore: EncryptedKeyStore
    ) {
        if (!keyStore.containsKey(jwk.keyID)) {
            keyStore.storeKey(jwk, jwk.keyID)
        }
    }

    private fun vcsToIterator(serializer: Json): Iterator<Pair<VerifiableCredential, VCMetadata>> {
        return VCIterator(vcs, vcsMetaInf, serializer)
    }

    private class VCIterator (
        val vcs: Map<String, String>,
        val vcsMetaInf: Map<String, VCMetadata>,
        val serializer: Json
    ) : Iterator<Pair<VerifiableCredential, VCMetadata>> {
        val jtis: Iterator<String> = vcs.keys.iterator()

        override fun hasNext(): Boolean {
            return jtis.hasNext()
        }

        override fun next(): Pair<VerifiableCredential, VCMetadata> {
            val jti = jtis.next()
            val rawToken = vcs[jti]!!
            val jwsToken = JwsToken.deserialize(rawToken)
            val verifiableCredentialContent = serializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawToken, verifiableCredentialContent)
            return Pair(vc, vcsMetaInf[jti]!!)
        }
    }
}
