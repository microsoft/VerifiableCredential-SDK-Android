package com.microsoft.portableIdentity.sdk.utilities

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.document.service.ServiceHubEndpoint
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class SerializationTest {
    private var actualDocument: IdentifierDocument = IdentifierDocument(
        context = "https://w3id.org/did/v1",
        id = "did:test:hub.id",
        created = "2019-07-15T22:36:00.881Z",
        publicKeys = listOf(
            IdentifierDocumentPublicKey(
                id = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                type = "RsaVerificationKey2018",
                controller = "did:test:hub.id",
                publicKeyJwk = JsonWebKey(
                    kty = "RSA",
                    kid = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                    alg = "RSA-OAEP",
                    key_ops = listOf("sign", "verify", "wrapKey", "unwrapKey", "encrypt", "decrypt"),
                    n = "uG76CgQGPSTx0ZuJBvof4ceNj4Taci3xaFpt_2hQeLhbjvE_N7SHFU86rFWxZMv_DP7h9cfDImp" +
                            "imbUpg3tmcd5jTsulwGHSQr4u1WfQXqN_BiGJ9EyGhIYTjPNBXODpZCsO62GksLlJi1xaZU" +
                            "_EobC98s3sUsdI_zkjnuTL2T2ar3kzP8Pj0WkSRf-2WE1gXLNW8fzB8Y7_gFPtdwuTx4EYH" +
                            "MEeuqZhzjPBtuw7PLrCbYm3EHx5BCNIhJag3cyDLMOHmp4xlof9_zNZQ5UpxOlJuRHNgz9o" +
                            "nthtm2fYS_R-ZBZH2JNhAkUsMHQFF5GAISAMkG877HOupBhRRn6VQybHqeVyzqfgKKpCHni" +
                            "ZACAZTp5zy5GhGVnik4qZcrSvZMLGscftz71zqV-ny9Ck5WIJ6gSGoGDwigJx3smt_seyYM" +
                            "xJUJjYF3NGzmzLALZwMWq4FNu21iBFMovzpb5aCcC-HQhVFyLSzkZS2-AEM-7TE0MMeWQcj" +
                            "pJCmOxgl0zrf7MFv5IDlco_hO4WRmFp9NIqewLDrS52fdN_yjnH3mKwnJYByomHhOnMNTTg" +
                            "oqrVOZzO59mOycz0Mx4rKTxyWcDwUrO8wb846m11JL06I-D5i7KBrQpHy8E0Yeabr5gWkdR" +
                            "rAc_9Ifox5vJ3lZzkBYHYq871xneyURPh9LZqP2E",
                    e = "AQAB"
                )
            )
        ),
        services = listOf(
            IdentityHubService(
                id = "#hubEndpoint",
                publicKey = "did:test:hub.id#HubSigningKey-RSA?9a1142b622c342f38d41b20b09960467",
                serviceEndpoint = ServiceHubEndpoint(listOf("https://beta.hub.microsoft.com/"))
            )
        )
    )

    @Test
    fun `serialize and deserialize an identity document`() {
        val serializedDocument = Serializer.stringify(IdentifierDocument.serializer(), actualDocument)
        val expectedDocument = Serializer.parse(IdentifierDocument.serializer(), serializedDocument)
        val serializedExpectedDocument = Serializer.stringify(IdentifierDocument.serializer(), expectedDocument)
        assertThat(serializedDocument).isEqualTo(serializedExpectedDocument)
    }
}