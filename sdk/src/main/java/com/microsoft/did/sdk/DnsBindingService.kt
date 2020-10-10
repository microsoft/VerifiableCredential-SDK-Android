// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.models.dnsBinding.DomainLinkageCredential
import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import com.microsoft.did.sdk.credential.service.protectors.RevocationResponseFormatter
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.DomainValidationException
import com.microsoft.did.sdk.util.controlflow.ResolverException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.Success
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DnsBindingService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val revocationResponseFormatter: RevocationResponseFormatter,
    private val resolver: Resolver,
    private val serializer: Serializer
) {
    suspend fun validateDomainBinding(rpDid: String): Result<Success> {
        val wellKnownConfigDocumentUrl = getDomainForRp(rpDid)
        val wellKnownConfigDocument = getWellKnownConfigDocument(wellKnownConfigDocumentUrl)
        return Result.Failure(DomainValidationException("$wellKnownConfigDocumentUrl is not bound to $rpDid"))
    }

    suspend fun getDomainForRp(rpDid: String): String {
        return when (val didDocument = resolver.resolve(rpDid)) {
            is Result.Success -> "testsite.com"
            /*didDocument.payload.service.firstOrNull()?.endpoint ?: throw MissingDomainBindingDocumentEndpointException(
                "Endpoint to locate well known configuration document is missing"
            )*/
            is Result.Failure -> throw ResolverException("Unable to resolve $rpDid", didDocument.payload)
        }
    }

    private suspend fun getWellKnownConfigDocument(configDocumentUrl: String) {

    }

    private fun validateWellKnownConfigDocument() {
    }

    fun deserializeConfigDocument(docAsJwt: String): List<DomainLinkageCredential> {
        val jwt = JwsToken.deserialize(docAsJwt, serializer)
        val response = serializer.parse(DnsBindingResponse.serializer(), jwt.content())
        return response.linked_dids
    }
}