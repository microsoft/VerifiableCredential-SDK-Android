// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.dnsBindingOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.LinkedDomainNotBoundException
import com.microsoft.did.sdk.util.controlflow.MissingLinkedDomainInDidException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnableToFetchWellKnownConfigDocument
import com.microsoft.did.sdk.util.controlflow.map
import com.microsoft.did.sdk.util.controlflow.runResultTry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsBindingService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {
    suspend fun fetchAndVerifyDnsBinding(relyingPartyDid: String, domainUrl: String): Result<Unit> {
        return runResultTry {
            if (domainUrl.isEmpty())
                Result.Failure(MissingLinkedDomainInDidException("Domain to locate well known configuration document is missing"))
            when (val wellKnownConfigDocument = getWellKnownConfigDocument(domainUrl)) {
                is Result.Success -> {
                    wellKnownConfigDocument.payload.linkedDids.forEach { linkedDid ->
                        val isDomainBound = jwtDomainLinkageCredentialValidator.validate(linkedDid, relyingPartyDid, domainUrl)
                        if (isDomainBound) Result.Success(Unit)
                    }
                }
                is Result.Failure -> Result.Failure(UnableToFetchWellKnownConfigDocument("Unable to fetch well-known config document from $domainUrl for DID $relyingPartyDid"))
            }
            Result.Failure(LinkedDomainNotBoundException("$domainUrl is not bound to $relyingPartyDid"))
        }
    }

    suspend fun getDomainUrlFromRelyingPartyDid(relyingPartyDid: String): Result<String> {
        val didDocument = resolver.resolve(relyingPartyDid)
        return didDocument.map { didDocument -> getFirstLinkedDomainsEndpoint(didDocument) }
    }

    private fun getFirstLinkedDomainsEndpoint(didDocument: IdentifierDocument): String {
        val linkedDomains = didDocument.service.filter { it.type == Constants.LINKED_DOMAINS_SERVICE_ENDPOINT }
        return if (linkedDomains.isEmpty())
            ""
        else
            linkedDomains.first().serviceEndpoint
    }

    private suspend fun getWellKnownConfigDocument(configDocumentUrl: String) =
        FetchWellKnownConfigDocumentNetworkOperation(configDocumentUrl, apiProvider).fire()
}