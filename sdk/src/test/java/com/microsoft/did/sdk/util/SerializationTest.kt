package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.nimbusds.jose.jwk.JWK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SerializationTest {
    private var actualDocument: IdentifierDocument =
        IdentifierDocument(
            id = "did:test:hub.id",
            verificationMethod = listOf(
                IdentifierDocumentPublicKey(
                    id = "#signingKey",
                    type = "Secp256k1VerificationKey2018",
                    controller = "did:test:hub.id",
                    publicKeyJwk = JWK.parse(
                        "{\"kty\": \"EC\"," +
                            "\"crv\": \"secp256k1\"," +
                            "\"x\": \"AEaA_TMpNsRwmZNwe70z2q_dz1rQ7G8gN0_UAydEMyU\"," +
                            "\"y\": \"ICzV5CiqZJeAS34tJ6t9AwKoe5dQpqlf25Eay5Stpco\"}"
                    )
                )
            )
        )
    private val receipt = """{
    "receipt": {
        "urn:pic:0f032399-e171-4ff1-ab82-87e4239dc3e5": "eyJraWQiOiJkaWQ6aW9uOkVpQ2ZlT2NpRWp3dXB3UlFzSkMzd01aenozX00zWElvNmJoeTdhSmtDRzZDQVE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRU1EUXdZMmxRYWtVeFIweHFMWEV5V21SeUxWSmFYelZsY1U4eU5GbERNRkk1YlRsRWQyWkhNa2RHUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVNeVJtUTVVRTkwZW1GTmNVdE1hRE5SVEZwMFdrNDNWMFJEUkhKamRrTjRlVE52ZGxORVJEaEtSR1ZSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUTJndGFURkRNVzFmTTJONFNHSk5NM3BYZW1SUmRFeHhNbkJ2UmxkYVgyNUZWRUpUYjBOaFQySlpUV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YekJtT1RkbFpXWmpJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1EweHNiM0pKYkd4Mk0yRldTa1JpWWtOeE0wVkhielUyYldWNlEzUkxXa1pHY1V0dlMzUlZjM0J6SWl3aWVTSTZJbWgxVkc1aVRFYzNNV1UwTkRORWVWSmtlVTVEWDNkZmMzcGFSMGhWWVVjeFVIZHNNSHBYYjBoMkxVRWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnXzBmOTdlZWZjIiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTZLIn0.eyJqdGkiOiIxYTlkOTI1ZjliMjM0NzY2YjI0N2JlNjg5NTBkMWU0NCIsImlzcyI6ImRpZDppb246RWlBaEl4bW5KRk5IZTRnX1ZJQk01a01QVlZBdmc4VmpFeHpVSWYzejFzbzllZz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xCYkc1cWJXdHJiV3ROY0RaU1pqSmlZakZsYmxKUGJtWnZMV3g1V0dGRGVsWjBabmhRTlMxNWJWQktVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUo1YzNsME1qa3phR2xLVUdWcFgyb3pTRVp1VEZSbVpTMXdZVWRHT1c4MWVXNDFZMFpWUjJGa2IwRkJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRbmx6ZVhReU9UTm9hVXBRWldsZmFqTklSbTVNVkdabExYQmhSMFk1YnpWNWJqVmpSbFZIWVdSdlFVRWlMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pVkY5clgzTnBaMjVmYVV3MFEzRkZSa2xmTVNJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2lTMWxsUVc4NFoyVlJhRkpVU1hOWE9FUlZabTEzUkROSFJuSjJjMDVLVTNsVVpEaHhhbDlMUzA5ZmR5SXNJbmtpT2lKeGRsbHhja1pwUkhNeVUyZFhWVm81Y0VWRmVYVjJaVFIwWTNReVkxcE5PV1ZYUWxKc00wcEJVREIzSW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEiLCJjcmVkZW50aWFsU3RhdHVzIjp7ImlkIjoidXJuOnBpYzowZjAzMjM5OS1lMTcxLTRmZjEtYWI4Mi04N2U0MjM5ZGMzZTUiLCJyZWFzb24iOiJ0ZXN0aW5nIHJldm9jYXRpb24iLCJzdGF0dXMiOiJyZXZva2VkIn0sInJwIjpbXSwiaWF0IjoxNTk0NzEzMzcwfQ.MEYCIQDF6WTCaqWXA_EIUBDUJH4BfZhGc3bfgtQju99smp4-HgIhAIpTVRAa8zUyWgHsKjbkUhGpLY5MiBn72AbQqRH5Gcwl"
    }
}"""
    private val expectedReceipt =
        "eyJraWQiOiJkaWQ6aW9uOkVpQ2ZlT2NpRWp3dXB3UlFzSkMzd01aenozX00zWElvNmJoeTdhSmtDRzZDQVE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRU1EUXdZMmxRYWtVeFIweHFMWEV5V21SeUxWSmFYelZsY1U4eU5GbERNRkk1YlRsRWQyWkhNa2RHUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVNeVJtUTVVRTkwZW1GTmNVdE1hRE5SVEZwMFdrNDNWMFJEUkhKamRrTjRlVE52ZGxORVJEaEtSR1ZSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUTJndGFURkRNVzFmTTJONFNHSk5NM3BYZW1SUmRFeHhNbkJ2UmxkYVgyNUZWRUpUYjBOaFQySlpUV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YekJtT1RkbFpXWmpJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1EweHNiM0pKYkd4Mk0yRldTa1JpWWtOeE0wVkhielUyYldWNlEzUkxXa1pHY1V0dlMzUlZjM0J6SWl3aWVTSTZJbWgxVkc1aVRFYzNNV1UwTkRORWVWSmtlVTVEWDNkZmMzcGFSMGhWWVVjeFVIZHNNSHBYYjBoMkxVRWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnXzBmOTdlZWZjIiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTZLIn0.eyJqdGkiOiIxYTlkOTI1ZjliMjM0NzY2YjI0N2JlNjg5NTBkMWU0NCIsImlzcyI6ImRpZDppb246RWlBaEl4bW5KRk5IZTRnX1ZJQk01a01QVlZBdmc4VmpFeHpVSWYzejFzbzllZz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xCYkc1cWJXdHJiV3ROY0RaU1pqSmlZakZsYmxKUGJtWnZMV3g1V0dGRGVsWjBabmhRTlMxNWJWQktVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUo1YzNsME1qa3phR2xLVUdWcFgyb3pTRVp1VEZSbVpTMXdZVWRHT1c4MWVXNDFZMFpWUjJGa2IwRkJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRbmx6ZVhReU9UTm9hVXBRWldsZmFqTklSbTVNVkdabExYQmhSMFk1YnpWNWJqVmpSbFZIWVdSdlFVRWlMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pVkY5clgzTnBaMjVmYVV3MFEzRkZSa2xmTVNJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2lTMWxsUVc4NFoyVlJhRkpVU1hOWE9FUlZabTEzUkROSFJuSjJjMDVLVTNsVVpEaHhhbDlMUzA5ZmR5SXNJbmtpT2lKeGRsbHhja1pwUkhNeVUyZFhWVm81Y0VWRmVYVjJaVFIwWTNReVkxcE5PV1ZYUWxKc00wcEJVREIzSW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEiLCJjcmVkZW50aWFsU3RhdHVzIjp7ImlkIjoidXJuOnBpYzowZjAzMjM5OS1lMTcxLTRmZjEtYWI4Mi04N2U0MjM5ZGMzZTUiLCJyZWFzb24iOiJ0ZXN0aW5nIHJldm9jYXRpb24iLCJzdGF0dXMiOiJyZXZva2VkIn0sInJwIjpbXSwiaWF0IjoxNTk0NzEzMzcwfQ.MEYCIQDF6WTCaqWXA_EIUBDUJH4BfZhGc3bfgtQju99smp4-HgIhAIpTVRAa8zUyWgHsKjbkUhGpLY5MiBn72AbQqRH5Gcwl"
    private val expectedKey = "urn:pic:0f032399-e171-4ff1-ab82-87e4239dc3e5"

    @Test
    fun `serialize and deserialize an identity document`() {
        val serializedDocument = defaultTestSerializer.encodeToString(IdentifierDocument.serializer(), actualDocument)
        val expectedDocument = defaultTestSerializer.decodeFromString(IdentifierDocument.serializer(), serializedDocument)
        val serializedExpectedDocument = defaultTestSerializer.encodeToString(IdentifierDocument.serializer(), expectedDocument)
        assertThat(serializedDocument).isEqualTo(serializedExpectedDocument)
    }

    @Test
    fun `deserialize revocation receipt with varying keys`() {
        val revokeReceipt = defaultTestSerializer.decodeFromString(RevocationServiceResponse.serializer(), receipt)
        val actualKey = revokeReceipt.receipt.keys.firstOrNull()
        assertThat(actualKey).isNotNull
        assertThat(actualKey).isEqualTo(expectedKey)
        val actualReceipt = revokeReceipt.receipt.values.firstOrNull()
        assertThat(actualReceipt).isNotNull
        assertThat(actualReceipt).isEqualTo(expectedReceipt)
    }

    @Test
    fun `testing polymorphic serialization`() {
        val issResponse = IssuanceServiceResponse("testvc")
        val expectedSerializedResult = """{"vc":"testvc"}"""
        val serialized = defaultTestSerializer.encodeToString(IssuanceServiceResponse.serializer(), issResponse)
        assertThat(serialized).isEqualTo(expectedSerializedResult)
    }
}