package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.keys.KeyType;
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.datasource.file.models.RawIdentity
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import java.security.KeyException
import javax.inject.Inject

class RestoreOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val cryptoOperations: CryptoOperations,
) {
    private fun restoreVerifiedCredential(
        jti: String,
        vcJwt: String
    ) {

    }

    private fun restoreIdentifier (
        identifierData: RawIdentity
    ): Identifier {
        val signingKeyRef = if (identifierData.signatureKey != null) {
            importKey(identifierData.signatureKey, identifierData.alias, Constants.SIGNATURE_KEYREFERENCE)
        } else {
            ""
        }
        val recoveryKeyRef = importKey(identifierData.recoveryKey, identifierData.alias, Constants.RECOVERY_KEYREFERENCE)
        val updateKeyRef = importKey(identifierData.updateKey, identifierData.alias, Constants.UPDATE_KEYREFERENCE)

        val id = Identifier(
            identifierData.id,
            identifierData.alias,
            signingKeyRef,
            "",
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        identifierRepository.insert(id)
        return id
    }

    private fun importKey(
        jwk: JsonWebKey,
        alias: String,
        keyReferenceSuffix: String
    ): String {
        val key = when (jwk.kty) {
            KeyType.RSA.value -> {
                RsaPrivateKey(jwk)
            }
            KeyType.EllipticCurve.value -> {
                EllipticCurvePrivateKey(jwk)
            }
            else -> {
                throw KeyException("Unsupported key type ${jwk.kty}")
            }
        }
        val knownKeys = cryptoOperations.keyStore.list().entries.filter {
            it.value.kids.contains(key.kid)
        }
        if (knownKeys.isNotEmpty()) {
            return knownKeys.first().key
        }

        val keyRef = "${alias}_${keyReferenceSuffix}"
        cryptoOperations.keyStore.save(keyRef, key);
        return keyRef;
    }

}