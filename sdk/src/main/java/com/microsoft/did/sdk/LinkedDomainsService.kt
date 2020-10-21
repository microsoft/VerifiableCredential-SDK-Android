// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.DomainNotLinkedException
import com.microsoft.did.sdk.util.controlflow.MissingLinkedDomainsInDidException
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
    suspend fun fetchAndVerifyLinkedDomains(relyingPartyDid: String, domainUrl: String): Result<Unit> {
        return runResultTry {
            if (domainUrl.isEmpty())
                Result.Failure(MissingLinkedDomainsInDidException("Domain to locate well known configuration document is missing in the service endpoint of $relyingPartyDid"))
            val wellKnownConfigDocument = getWellKnownConfigDocument(domainUrl).abortOnError()
            wellKnownConfigDocument.linkedDids.forEach { linkedDid ->
                val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(linkedDid, relyingPartyDid, domainUrl)
                if (isDomainLinked) return@runResultTry Result.Success(Unit)
            }
            Result.Failure(DomainNotLinkedException("$domainUrl is not linked to $relyingPartyDid"))
        }
    }

    suspend fun getDomainUrlFromRelyingPartyDid(relyingPartyDid: String): Result<String> {
        val didDocument = resolver.resolve(relyingPartyDid)
        return didDocument.map { getFirstLinkedDomainServiceEndpoint(it) }
    }

    private fun getFirstLinkedDomainServiceEndpoint(didDocument: IdentifierDocument): String {
        val linkedDomainsServices = didDocument.service.filter { it.type == Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE }
        return if (linkedDomainsServices.isEmpty()) "" else linkedDomainsServices.first().serviceEndpoint.first()
    }

    private suspend fun getWellKnownConfigDocument(domainUrl: String) = FetchWellKnownConfigDocumentNetworkOperation(
        domainUrl,
        apiProvider
    ).fire()
}