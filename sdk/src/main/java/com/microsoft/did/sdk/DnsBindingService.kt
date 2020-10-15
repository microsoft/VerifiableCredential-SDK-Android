// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.dnsBindingOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.LinkedDomainNotBoundException
import com.microsoft.did.sdk.util.controlflow.MissingLinkedDomainInDidException
import com.microsoft.did.sdk.util.controlflow.ResolverException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnableToFetchWellKnownConfigDocument
import com.microsoft.did.sdk.util.controlflow.runResultTry
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsBindingService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {
    suspend fun verifyDnsBinding(rpDid: String): Result<String> {
        return runResultTry {
            val wellKnownConfigDocumentUrl = getDomainFromRpDid(rpDid)
            validateConfigDoc(wellKnownConfigDocumentUrl, rpDid)
        }
    }

    suspend fun temporaryBindingCheck(clientId: String, rpDid: String): Result<String> {
        return runResultTry {
            val wellKnownConfigDocumentUrl = getDomainName(clientId)
            validateConfigDoc(wellKnownConfigDocumentUrl, rpDid)
        }
    }

    fun getDomainName(url: String): String {
        val uri = URI(url)
        val domain = uri.host
        return if (domain.startsWith("www."))
            "https://" + domain.substring(4)
        else {
            val start = domain.indexOfFirst({ it == '.'})
            "https://" + domain.substring(start+1)
        }
    }

    suspend fun validateConfigDoc(wellKnownConfigDocumentUrl: String, rpDid: String): Result<String> {
        when (val wellKnownConfigDocument = getWellKnownConfigDocument(wellKnownConfigDocumentUrl)) {
            is Result.Success -> {
                wellKnownConfigDocument.payload.linked_dids.forEach {
                    val isDomainBound = jwtDomainLinkageCredentialValidator.validate(it, rpDid, wellKnownConfigDocumentUrl)
                    if (isDomainBound) return Result.Success(wellKnownConfigDocumentUrl)
                }
            }
            is Result.Failure -> return Result.Failure(UnableToFetchWellKnownConfigDocument("Unable to fetch well-known config document from $wellKnownConfigDocumentUrl for DID $rpDid"))
        }
        return Result.Failure(LinkedDomainNotBoundException("$wellKnownConfigDocumentUrl is not bound to $rpDid"))
    }

    suspend fun getDomainFromRpDid(rpDid: String): String {
        return when (val didDocument = resolver.resolve(rpDid)) {
            is Result.Success -> {
                if (didDocument.payload.service == null) throw MissingLinkedDomainInDidException("Domain to locate well known configuration document is missing")
                val linkedDomains = didDocument.payload.service.filter { it.type == Constants.LINKED_DOMAINS_SERVICE_ENDPOINT }
                if (linkedDomains.isEmpty())
                    throw MissingLinkedDomainInDidException("Domain to locate well known configuration document is missing")
                else
                    linkedDomains.first().endpoint
            }
            is Result.Failure -> throw ResolverException("Unable to resolve $rpDid", didDocument.payload)
        }
    }

    private suspend fun getWellKnownConfigDocument(configDocumentUrl: String) =
        FetchWellKnownConfigDocumentNetworkOperation(configDocumentUrl, apiProvider).fire()
}