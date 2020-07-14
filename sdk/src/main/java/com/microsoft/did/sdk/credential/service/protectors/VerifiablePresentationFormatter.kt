package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.RequestedVchMapping
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationDescriptor
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifiablePresentationFormatter @Inject constructor(
    private val serializer: Serializer,
    private val signer: TokenSigner
) {

    // only support one VC per VP
    fun createPresentation(
        requestedVchMapping: RequestedVchMapping,
        audience: String,
        responder: Identifier
    ): String {
        val vp = VerifiablePresentationDescriptor(
            verifiableCredential = listOf(requestedVchMapping.second.verifiableCredential.raw),
            context = listOf(Constants.VP_CONTEXT_URL),
            type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
        )

        val (iat, exp: Long?) = createIatAndExp(requestedVchMapping.first.validityInterval)
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