package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.portableIdentity.sdk.auth.models.verifiablePresentation.VerifiablePresentationDescriptor
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifiablePresentationFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer,
    private val signer: TokenSigner
)  {

    // only support one VC per VP
    fun createPresentation(verifiableCredentials: List<VerifiableCredential>,
                           audience: String,
                           responder: Identifier,
                           iat: Long,
                           exp: Long): String {
        val vp =
            VerifiablePresentationDescriptor(
                verifiableCredential = verifiableCredentials.map { it.raw },
                context = listOf(Constants.VP_CONTEXT_URL),
                type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
            )
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val contents =
            VerifiablePresentationContent(
                jti = jti,
                vp = vp,
                iss = did,
                iat = iat,
                nbf = iat,
                exp = exp,
                aud = audience
            )
        val serializedContents = serializer.stringify(VerifiablePresentationContent.serializer(), contents)
        return signer.signWithIdentifier(serializedContents, responder)
    }
}