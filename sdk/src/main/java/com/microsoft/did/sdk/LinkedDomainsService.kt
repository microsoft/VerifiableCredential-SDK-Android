// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.map
import com.microsoft.did.sdk.util.controlflow.runResultTry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkedDomainsService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {
    suspend fun fetchAndVerifyLinkedDomains(relyingPartyDid: String): Result<LinkedDomainResult<String>> {
        return runResultTry {
            val domainUrl = getDomainUrlFromRelyingPartyDid(relyingPartyDid).abortOnError()
            if(domainUrl.isEmpty())
                return@runResultTry Result.Success(LinkedDomainResult.Missing(""))
            val wellKnownConfigDocument = getWellKnownConfigDocument(domainUrl).abortOnError()
            wellKnownConfigDocument.linkedDids.forEach { linkedDid ->
                val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(linkedDid, relyingPartyDid, domainUrl)
                if (isDomainLinked) return@runResultTry Result.Success(LinkedDomainResult.Verified(domainUrl))
            }
            Result.Success(LinkedDomainResult.UnVerified(domainUrl))
        }
    }

    private suspend fun getDomainUrlFromRelyingPartyDid(relyingPartyDid: String): Result<String> {
        val didDocumentResult = resolver.resolve(relyingPartyDid)
        return didDocumentResult.map { didDocument ->
            val linkedDomainsServices = didDocument.service.filter { it.type == Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE }
            if (linkedDomainsServices.isEmpty()) ""
            else linkedDomainsServices.first().serviceEndpoint.first()

        }
    }

    private suspend fun getWellKnownConfigDocument(domainUrl: String) = FetchWellKnownConfigDocumentNetworkOperation(
        domainUrl,
        apiProvider
    ).fire()
}