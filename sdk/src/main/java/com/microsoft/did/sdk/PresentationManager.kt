// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.net.Uri
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationManager @Inject constructor(
    private val identifierManager: IdentifierManager,
    private val vcRepository: VerifiableCredentialRepository,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator
) {

    suspend fun getPresentationRequest(stringUri: String): Result<PresentationRequest> {
        return runResultTry {
            val uri = verifyUri(stringUri)
            val requestToken = getPresentationRequestToken(uri).abortOnError()
            val tokenContents =
                serializer.parse(
                    PresentationRequestContent.serializer(),
                    JwsToken.deserialize(requestToken, serializer).content()
                )
            val request = PresentationRequest(requestToken, tokenContents)
            isRequestValid(request).abortOnError()
            Result.Success(request)
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (url.scheme != Constants.DEEP_LINK_SCHEME && url.host != Constants.DEEP_LINK_HOST) {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestToken(uri: Uri): Result<String> {
        val serializedToken = uri.getQueryParameter("request")
        if (serializedToken != null) {
            return Result.Success(serializedToken)
        }
        val requestUri = uri.getQueryParameter("request_uri")
        if (requestUri != null) {
            return vcRepository.getRequest(requestUri)
        }
        return Result.Failure(PresentationException("No query parameter 'request' nor 'request_uri' is passed."))
    }

    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
    }

    suspend fun sendPresentationResponse(
        response: PresentationResponse,
        enablePairwise: Boolean
    ): Result<Unit> {
        return runResultTry {
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            if (enablePairwise) {
                val pairwiseIdentifier =
                    identifierManager.createPairwiseIdentifier(masterIdentifier, response.request.entityIdentifier).abortOnError()
                val vcRequestedMapping = exchangeVcsInPresentationRequest(response, pairwiseIdentifier).abortOnError()
                vcRepository.sendPresentationResponse(response, pairwiseIdentifier, vcRequestedMapping).abortOnError()
            } else {
                val vcRequestedMapping = response.requestedVcPresentationSubmissionMap
                vcRepository.sendPresentationResponse(response, masterIdentifier, vcRequestedMapping).abortOnError()
            }
            Result.Success(Unit)
        }
    }

    private suspend fun exchangeVcsInPresentationRequest(
        response: PresentationResponse,
        pairwiseIdentifier: Identifier
    ): Result<RequestedVcPresentationSubmissionMap> {
        return runResultTry {
            val exchangedVcMap = response.requestedVcPresentationSubmissionMap.mapValues {
                val owner = identifierManager.getIdentifierById(it.value.contents.sub).abortOnError()
                vcRepository.getExchangedVerifiableCredential(it.value, owner, pairwiseIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcPresentationSubmissionMap)
        }
    }
}