package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.AuthenticationConstants
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
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
class OidcResponseFormatter@Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String
) {

    fun formContents(response: OidcResponse, responderDid: String, useKey: String = signatureKeyReference, expiresIn: Int = AuthenticationConstants.RESPONSE_EXPIRATION_IN_MINUTES): OidcResponseContent {
        val requestContent = response.getRequestContents()
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(useKey).getKey()
        val jti = UUID.randomUUID().toString()

        return OidcResponseContent(
            sub = key.getThumbprint(cryptoOperations, Sha.Sha256),
            aud = requestContent.redirectUrl,
            nonce = requestContent.nonce,
            did = responderDid,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = requestContent.state,
            jti = jti
        )
    }

    private fun createIatAndExp(expiresIn: Int = AuthenticationConstants.RESPONSE_EXPIRATION_IN_MINUTES): Pair<Long, Long> {
        val currentTime = Date().time
        val expiration = currentTime + 1000 * 60 * expiresIn
        val exp = floor(expiration / 1000f).toLong()
        val iat = floor( currentTime / 1000f).toLong()
        return Pair(iat, exp)
    }
}