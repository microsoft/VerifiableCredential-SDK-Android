package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.identifier.document.*

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class IdResponse constructor(
    val document: IdDoc
) {
    companion object {
        var microsoftIdentityHubDocument: IdentifierDoc = IdentifierDoc(
            publicKeys = listOf(
                IdentifierDocPublicKey(
                    id = "#key1",
                    type = "Secp256k1VerificationKey2018",
                    publicKeyHex = "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
                )
            )/*,
            services = listOf(
                IdHubService(
                    id = "IdentityHub",
                    publicKey = "#key1",
                    serviceEndpoint = UserHubEndpoint(listOf("did:bar:456", "did:zaz:789"))
                )
            )*/
        )
    }

    fun serialize(): String {
        return IdResponseToken.serialize(this)
    }
}
