package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationDescriptor
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifiablePresentationFormatter @Inject constructor(
    private val serializer: Serializer,
    private val signer: TokenSigner
) {

    // only support one VC per VP
    fun createPresentation(
        verifiableCredential: VerifiableCredential,
        validityInterval: Int,
        audience: String,
        responder: Identifier
    ): String {
        val vp = VerifiablePresentationDescriptor(
            verifiableCredential = listOf(verifiableCredential.raw),
            context = listOf(Constants.VP_CONTEXT_URL),
            type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
        )

        val (iat, exp: Long?) = createIatAndExp(validityInterval)
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val contents =
            VerifiablePresentationContent(
                jti = jti,
                vp = vp,
                iss = did,
                tokenIssuedTime = iat,
                tokenNotValidBefore = iat,
                tokenExpiryTime = exp,
                aud = audience
            )
        val serializedContents = serializer.stringify(VerifiablePresentationContent.serializer(), contents)
        return signer.signWithIdentifier(serializedContents, responder)
    }
}