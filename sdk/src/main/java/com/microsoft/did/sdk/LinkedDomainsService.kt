// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.did.sdk.util.controlflow.map
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.log.SdkLog
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkedDomainsService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {

    suspend fun fetchAndVerifyLinkedDomains(
        relyingPartyDid: String,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): Result<LinkedDomainResult> {
        return try {
            val verifiedDomains = verifyLinkedDomainsUsingResolver(relyingPartyDid, rootOfTrustResolver)
            Result.Success(verifiedDomains)
        } catch (ex: SdkException) {
            SdkLog.d(
                "Linked Domains verification using resolver failed with exception $ex. " +
                    "Verifying it using Well Known Document."
            )
            verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid)
        }
    }

    private suspend fun verifyLinkedDomainsUsingResolver(
        relyingPartyDid: String,
        rootOfTrustResolver: RootOfTrustResolver?
    ): LinkedDomainResult {
        rootOfTrustResolver ?: throw SdkException("Root of trust resolver is not configured")
        val linkedDomainResult = rootOfTrustResolver.resolve(relyingPartyDid)
        if (linkedDomainResult is LinkedDomainVerified) return linkedDomainResult
        else throw SdkException("Root of trust resolver did not return a verified domain")
    }

    private suspend fun verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid: String): Result<LinkedDomainResult> {
        return runResultTry {
            val domainUrls = getLinkedDomainsFromDid(relyingPartyDid).abortOnError()
            verifyLinkedDomains(domainUrls, relyingPartyDid)
        }
    }

    private suspend fun verifyLinkedDomains(domainUrls: List<String>, relyingPartyDid: String): Result<LinkedDomainResult> {
        return runResultTry {
            if (domainUrls.isEmpty())
                return@runResultTry Result.Success(LinkedDomainMissing)
            val domainUrl = domainUrls.first()
            val hostname = URL(domainUrl).host
            val wellKnownConfigDocumentResult = getWellKnownConfigDocument(domainUrl)
            if (wellKnownConfigDocumentResult is Result.Success) {
                val wellKnownConfigDocument = wellKnownConfigDocumentResult.payload
                wellKnownConfigDocument.linkedDids.forEach { linkedDidJwt ->
                    val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(linkedDidJwt, relyingPartyDid, domainUrl)
                    if (isDomainLinked) return@runResultTry Result.Success(LinkedDomainVerified(hostname))
                }
            } else SdkLog.d("Unable to fetch well-known config document from $domainUrl")
            Result.Success(LinkedDomainUnVerified(hostname))
        }
    }

    private suspend fun getLinkedDomainsFromDid(relyingPartyDid: String): Result<List<String>> {
        val didDocumentResult = resolver.resolve(relyingPartyDid)
        return didDocumentResult.map { didDocument ->
            val linkedDomainsServices =
                didDocument.service.filter { service -> service.type.equals(Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE, true) }
            linkedDomainsServices.map { it.serviceEndpoint }.flatten()
        }
    }

    private suspend fun getWellKnownConfigDocument(domainUrl: String) = FetchWellKnownConfigDocumentNetworkOperation(
        domainUrl,
        apiProvider
    ).fire()
}