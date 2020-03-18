package com.microsoft.portableIdentity.sdk.auth.models.siop

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SIOPResponseContent(
    @Required
    val iss: String = OidcResponse.SELFISSUED,
    val sub: String, // thumbprint (sha-256)
    val aud: String,
    val nonce: String,
    val did: String?,
    @SerialName("sub_jwk")
    val subJwk: JsonWebKey,
    val iat: Int,
    val exp: Int,

    // NON COMPLIANT STATE
    val state: String? = null,
    @SerialName("_claim_names")
    val claimNames: Map<String, String>? = null,
    @SerialName("_claim_sources")
    val claimSources: Map<String, List<Map<String, String>>>? = null,

    // optional parameters
    val schema: String? = null
) : ResponseContent {

    override fun addSignerParams(signer: Signer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stringify(): String {
        return Serializer.stringify(SIOPResponseContent.serializer(), this)
    }

    companion object {
        fun create(requestContent: RequestContent, credentials: List<Credential>) : SIOPResponseContent {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}