// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.PayloadGenerator
import com.microsoft.portableIdentity.sdk.identifier.models.PatchData
import com.microsoft.portableIdentity.sdk.identifier.models.SuffixData
import com.microsoft.portableIdentity.sdk.repository.PortableIdentityRepository
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.byteArrayToString
import javax.inject.Inject
import javax.inject.Named
import kotlin.random.Random

/**
 * Registrar implementation for the Sidetree network
 * @class
 * @implements IRegistrar
 * @param registrarUrl to the registration endpoint
 * @param identityRepository repository to perform portable identity related operations in network/database
 */
class SidetreeRegistrar @Inject constructor(@Named("registrationUrl") private val baseUrl: String, private val identityRepository: PortableIdentityRepository) :
    Registrar() {

    override suspend fun register(
        signatureKeyReference: String,
        encryptionKeyReference: String,
        recoveryKeyReference: String,
        cryptoOperations: CryptoOperations
    ): Identifier {
        val alias = Base64Url.encode(Random.nextBytes(16))
        val personaEncKeyRef = "$alias.$encryptionKeyReference"
        val personaSigKeyRef = "$alias.$signatureKeyReference"
        val personaRecKeyRef = "$alias.$recoveryKeyReference"
        val payloadGenerator = PayloadGenerator(
            cryptoOperations,
            signatureKeyReference,
            encryptionKeyReference,
            recoveryKeyReference
        )
        val registrationDocumentEncoded = payloadGenerator.generateCreatePayload(alias)
        val registrationDocument =
            Serializer.parse(RegistrationDocument.serializer(), byteArrayToString(Base64Url.decode(registrationDocumentEncoded)))

        val uniqueSuffix = payloadGenerator.computeUniqueSuffix(registrationDocument.suffixData)
        val portableIdentity = "did:${Constants.METHOD_NAME}:test:$uniqueSuffix"

        //Resolves the created long form identifier as validation before saving it
        val identifier = "$portableIdentity?${Constants.INITIAL_STATE_LONGFORM}=$registrationDocumentEncoded"
        val identifierDocument = identityRepository.resolveIdentifier(baseUrl, identifier)

        val patchDataJson = byteArrayToString(Base64Url.decode(registrationDocument.patchData))
        val nextUpdateCommitmentHash = Serializer.parse(PatchData.serializer(), patchDataJson).nextUpdateCommitmentHash
        val suffixDataJson = byteArrayToString(Base64Url.decode(registrationDocument.suffixData))
        val nextRecoveryCommitmentHash = Serializer.parse(SuffixData.serializer(), suffixDataJson).nextRecoveryCommitmentHash

        val longformIdentifier =
            Identifier(
                identifier,
                alias,
                personaSigKeyRef,
                personaEncKeyRef,
                personaRecKeyRef,
                nextUpdateCommitmentHash,
                nextRecoveryCommitmentHash,
                identifierDocument!!,
                Constants.IDENTITY_SECRET_KEY_NAME
            )
        identityRepository.insert(longformIdentifier)
        return identityRepository.queryById(identifier)
    }
}