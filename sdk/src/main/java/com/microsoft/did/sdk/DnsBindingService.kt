// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.models.dnsBinding.DomainLinkageCredential
import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.dnsBindingOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.DomainValidationException
import com.microsoft.did.sdk.util.controlflow.IdNotMatchingException
import com.microsoft.did.sdk.util.controlflow.MissingIssuanceDateException
import com.microsoft.did.sdk.util.controlflow.ResolverException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsBindingService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val serializer: Serializer
) {
    suspend fun validateDomainBinding(rpDid: String): Result<Unit> {
        return runResultTry {
            val wellKnownConfigDocumentUrl = getDomainForRp(rpDid)
            val docJwt =
                "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2NmM2EzMGJmIn0.eyJAY29udGV4dCI6Imh0dHBzOi8vaWRlbnRpdHkuZm91bmRhdGlvbi8ud2VsbC1rbm93bi9jb250ZXh0cy9kaWQtY29uZmlndXJhdGlvbi12MC4wLmpzb25sZCIsImxpbmtlZF9kaWRzIjpbeyJzdWIiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDIyNzM2NDUsInZjIjp7IkBjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIiwiaHR0cHM6Ly9pZGVudGl0eS5mb3VuZGF0aW9uLy53ZWxsLWtub3duL2NvbnRleHRzL2RpZC1jb25maWd1cmF0aW9uLXYwLjAuanNvbmxkIl0sImlzc3VlciI6ImRpZDppb246RWlESWpyYXFpMjZPSm1HM3pNNzRMZjBHNmNFalBZc0NvVUtwMVdPWDNPQUV3Zz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xFVUVsZlNFSk1UVWhYZGpaT1YySTVabWcwVTBFemRUbHdkVVJhV2xSS1R6WlBlRUpzVFdrdGMzQkxVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUpRV1RVMGFHeElaRGh2ZEdaUFMxcGxNbXcwWW1oM2JWZzVOR2R0VG5Nd1EzVjVibmw1YVZkNVluQlJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBSR1UzZDJweFFVSmhZbU5xYkZabU4yNWpSSGxJYkdoUlpUTmhNbWRUUWtac1ltbEpZMnBvU1VOdE9XY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblgyTm1NMkV6TUdKbUlpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpQYVZoYU16UklTRkZ2VFRKRmF6UlBVMFJ4ZWpWUlZqZDNSWGhKVUd0c09XSmFXSGxHVDFKbmRsTlpJaXdpZVNJNklrZDBTSE5KYUZObWVqaGFjbkpFUVc5UVEybzFUMGhhWDJkb1ptUXpXR3BXVFdwb1RHOXpObFprYVRnaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsImlzc3VhbmNlRGF0ZSI6IjIwMjAtMTAtMDlUMjA6MDA6NDUuMzExWiIsInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJEb21haW5MaW5rYWdlQ3JlZGVudGlhbCJdLCJjcmVkZW50aWFsU3ViamVjdCI6eyJpZCI6ImRpZDppb246RWlESWpyYXFpMjZPSm1HM3pNNzRMZjBHNmNFalBZc0NvVUtwMVdPWDNPQUV3Zz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xFVUVsZlNFSk1UVWhYZGpaT1YySTVabWcwVTBFemRUbHdkVVJhV2xSS1R6WlBlRUpzVFdrdGMzQkxVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUpRV1RVMGFHeElaRGh2ZEdaUFMxcGxNbXcwWW1oM2JWZzVOR2R0VG5Nd1EzVjVibmw1YVZkNVluQlJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBSR1UzZDJweFFVSmhZbU5xYkZabU4yNWpSSGxJYkdoUlpUTmhNbWRUUWtac1ltbEpZMnBvU1VOdE9XY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblgyTm1NMkV6TUdKbUlpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpQYVZoYU16UklTRkZ2VFRKRmF6UlBVMFJ4ZWpWUlZqZDNSWGhKVUd0c09XSmFXSGxHVDFKbmRsTlpJaXdpZVNJNklrZDBTSE5KYUZObWVqaGFjbkpFUVc5UVEybzFUMGhhWDJkb1ptUXpXR3BXVFdwb1RHOXpObFprYVRnaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsIm9yaWdpbiI6Ind3dy5nb29nbGUuY29tIn19fV19.MXxIIJHPTHQzu8UIR-39SezOnSU1hMfzHxWkUAoKDhO5BIG5f2awuX7ooRmuBpevm4VBPqsVVSA9aVwjqQ9nmA"
            val wellKnownConfigDocument = /*getWellKnownConfigDocument(wellKnownConfigDocumentUrl)*/ deserializeConfigDocument(docJwt)
            validateWellKnownConfigDocument(wellKnownConfigDocument.first())
            Result.Failure(DomainValidationException("$wellKnownConfigDocumentUrl is not bound to $rpDid"))
            Result.Success(Unit)
        }
    }

    private suspend fun getDomainForRp(rpDid: String): String {
        return when (val didDocument = resolver.resolve(rpDid)) {
            is Result.Success -> "testsite.com"
            /*{
                val linkedDomains = didDocument.payload.service.filter { it.type == "LinkedDomains" }
                if (linkedDomains.isEmpty())
                    throw MissingDomainBindingDocumentEndpointException(
                        "Endpoint to locate well known configuration document is missing"
                    )
                else
                    linkedDomains.first().endpoint
            }*/
            is Result.Failure -> throw ResolverException("Unable to resolve $rpDid", didDocument.payload)
        }
    }

    private suspend fun getWellKnownConfigDocument(configDocumentUrl: String) =
        FetchWellKnownConfigDocumentNetworkOperation(configDocumentUrl, apiProvider).fire()

    private fun validateWellKnownConfigDocument(domainLinkageCredential: DomainLinkageCredential) {
        if (domainLinkageCredential.sub != domainLinkageCredential.vc.credentialSubject.id)
            throw IdNotMatchingException("Subject DID doesn't match credential subject DID")
        if (domainLinkageCredential.iss != domainLinkageCredential.vc.credentialSubject.id)
            throw IdNotMatchingException("Issuer DID doesn't match credential subject DID")
        if (domainLinkageCredential.vc.issuanceDate.isNullOrEmpty())
            throw MissingIssuanceDateException("Issuance Date is missing in Domain Linkage Credential")
    }

    private fun verifyDomainBinding() {

    }

    fun deserializeConfigDocument(docAsJwt: String): List<DomainLinkageCredential> {
        val jwt = JwsToken.deserialize(docAsJwt, serializer)
        val response = serializer.parse(DnsBindingResponse.serializer(), jwt.content())
        return response.linked_dids
    }
}