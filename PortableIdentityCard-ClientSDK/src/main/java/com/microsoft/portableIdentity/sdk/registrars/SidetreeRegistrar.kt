// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.SidetreePayloadProcessor
import com.microsoft.portableIdentity.sdk.identifier.models.payload.PatchData
import com.microsoft.portableIdentity.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.portableIdentity.sdk.identifier.models.payload.SuffixData
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
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
 * @param identifierRepository repository to perform portable identity related operations in network/database
 */
class SidetreeRegistrar @Inject constructor(@Named("registrationUrl") private val baseUrl: String, private val identifierRepository: IdentifierRepository) :
    Registrar() {

    override suspend fun register(
        signatureKeyReference: String,
        recoveryKeyReference: String,
        cryptoOperations: CryptoOperations
    ): Identifier {
        val alias = Base64Url.encode(Random.nextBytes(16))
        val personaSigKeyRef = "$alias.$signatureKeyReference"
        val personaRecKeyRef = "$alias.$recoveryKeyReference"
        val payloadGenerator = SidetreePayloadProcessor(
            cryptoOperations,
            signatureKeyReference,
            recoveryKeyReference
        )
        val registrationDocumentEncoded = payloadGenerator.generateCreatePayload(alias)
        val registrationDocument =
            Serializer.parse(RegistrationPayload.serializer(), byteArrayToString(Base64Url.decode(registrationDocumentEncoded)))

        val uniqueSuffix = payloadGenerator.computeUniqueSuffix(registrationDocument.suffixData)
        val identifierShortForm = "did:${Constants.METHOD_NAME}:test:$uniqueSuffix"

        //TODO: Remove this when long form is finalized. Validates created long form create payload and identifier before saving it by resolving it
        val identifierLongForm = "$identifierShortForm?${Constants.INITIAL_STATE_LONGFORM}=$registrationDocumentEncoded"
//        val identifierDocument = identifierRepository.resolveIdentifier(baseUrl, identifierLongForm)

        val patchDataJson = byteArrayToString(Base64Url.decode(registrationDocument.patchData))
        val nextUpdateCommitmentHash = Serializer.parse(PatchData.serializer(), patchDataJson).nextUpdateCommitmentHash
        val suffixDataJson = byteArrayToString(Base64Url.decode(registrationDocument.suffixData))
        val nextRecoveryCommitmentHash = Serializer.parse(SuffixData.serializer(), suffixDataJson).nextRecoveryCommitmentHash

        val longformIdentifier =
            Identifier(
                identifierLongForm,
                alias,
                personaSigKeyRef,
                //TODO: Since we know encryption is coming in the future, do we want to add encryption key now so that we don't have to modify the table later.
                "",
                personaRecKeyRef,
                nextUpdateCommitmentHash,
                nextRecoveryCommitmentHash,
//                identifierDocument!!,
                Constants.IDENTIFIER_SECRET_KEY_NAME
            )
        identifierRepository.insert(longformIdentifier)
        return longformIdentifier
    }
}