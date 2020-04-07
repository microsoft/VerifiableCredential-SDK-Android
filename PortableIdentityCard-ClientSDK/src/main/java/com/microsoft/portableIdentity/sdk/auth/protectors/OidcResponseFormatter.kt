/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.attestationBindings.PicBinding
import com.microsoft.portableIdentity.sdk.auth.models.oidc.AttestationResponse
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
class OidcResponseFormatter @Inject constructor(
        private val cryptoOperations: CryptoOperations,
        @Named("signatureKeyReference") private val signatureKeyReference: String
) {

    fun formContents(response: OidcResponse, responderDid: String, useKey: String = signatureKeyReference, expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): OidcResponseContent {
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(useKey).getKey()
        val jti = UUID.randomUUID().toString()

        var contract: String? = null
        var nonce: String? = null
        var state: String? = null

        when (response) {
            is IssuanceResponse -> {
                contract = response.contractUrl
            }
            is PresentationResponse -> {
                nonce = response.nonce
                state = response.state
            }
        }

        val attestationResponse = createAttestationResponse(response)

        return OidcResponseContent(
                sub = key.getThumbprint(cryptoOperations, Sha.Sha256),
                aud = response.audience,
                nonce = nonce,
                did = responderDid,
                subJwk = key.toJWK(),
                iat = iat,
                exp = exp,
                state = state,
                jti = jti,
                contract = contract,
                attestations = attestationResponse
        )
    }

    private fun createAttestationResponse(response: OidcResponse): AttestationResponse? {
        var selfIssuedAttestations: Map<String, String>? = null
        var tokenAttestations: Map<String, String>? = null
        if (response is IssuanceResponse) {
            if (!response.getIdTokenBindings().isNullOrEmpty()) {
                tokenAttestations = response.getIdTokenBindings()
            }
            if (!response.getSelfIssuedClaimBindings().isNullOrEmpty()) {
                selfIssuedAttestations = response.getSelfIssuedClaimBindings()
            }
        }
        val presentationAttestation = createPresentations(response.getCardBindings())
        return AttestationResponse(selfIssuedAttestations, tokenAttestations, presentationAttestation)
    }

    // TODO(wrap VC in a VP and map it to the type)
    private fun createPresentations(binding: List<PicBinding>): Map<String, String>? {
        return null
    }

    private fun createIatAndExp(expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): Pair<Long, Long> {
        val currentTime = Date().time
        val expiration = currentTime + 1000 * 60 * expiresIn
        val exp = floor(expiration / 1000f).toLong()
        val iat = floor(currentTime / 1000f).toLong()
        return Pair(iat, exp)
    }
}