// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.net.Uri
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.protectors.PresentationResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationService @Inject constructor(
    private val identifierManager: IdentifierManager,
    private val exchangeService: ExchangeService,
    private val serializer: Serializer,
    private val presentationRequestValidator: PresentationRequestValidator,
    private val apiProvider: ApiProvider,
    private val presentationResponseFormatter: PresentationResponseFormatter
) {
    suspend fun getRequest(stringUri: String): Result<PresentationRequest> {
        return runResultTry {
            val startLoad = System.nanoTime()
            val uri = verifyUri(stringUri)
            val requestToken = getPresentationRequestToken(uri).abortOnError()
            val tokenContents =
                serializer.parse(
                    PresentationRequestContent.serializer(),
                    JwsToken.deserialize(requestToken, serializer).content()
                )
            val request = PresentationRequest(requestToken, tokenContents)
            isRequestValid(request).abortOnError()
            val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
            SdkLog.v("mPerf - SDK load time (PRESENTATION}): ${loadTimeInMs}ms")
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
            return fetchRequest(requestUri)
        }
        return Result.Failure(PresentationException("No query parameter 'request' nor 'request_uri' is passed."))
    }

    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
    }

    private suspend fun fetchRequest(url: String) = FetchPresentationRequestNetworkOperation(url, apiProvider).fire()

    /**
     * Send a Presentation Response.
     *
     * @param response PresentationResponse to be formed, signed, and sent.
     * @param enablePairwise when true a pairwise identifier will be used for this communication,
     * otherwise the master identifier is used which may allow the relying party to correlate the user
     */
    suspend fun sendResponse(
        response: PresentationResponse,
        enablePairwise: Boolean = true
    ): Result<Unit> {
        return runResultTry {
            val startLoad = System.nanoTime()
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            if (enablePairwise) {
                val pairwiseIdentifier =
                    identifierManager.createPairwiseIdentifier(masterIdentifier, response.request.entityIdentifier).abortOnError()
                val vcRequestedMapping = exchangeVcsInPresentationRequest(response, pairwiseIdentifier).abortOnError()
                formAndSendResponse(response, pairwiseIdentifier, vcRequestedMapping).abortOnError()
            } else {
                val vcRequestedMapping = response.requestedVcPresentationSubmissionMap
                formAndSendResponse(response, masterIdentifier, vcRequestedMapping).abortOnError()
            }
            val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
            SdkLog.v("mPerf - SDK complete response time (PRESENTATION}): ${loadTimeInMs}ms")
            Result.Success(Unit)
        }
    }

    private suspend fun exchangeVcsInPresentationRequest(
        response: PresentationResponse,
        pairwiseIdentifier: Identifier
    ): Result<RequestedVcPresentationSubmissionMap> {
        return runResultTry {
            val startLoad = System.nanoTime()
            val exchangedVcMap = response.requestedVcPresentationSubmissionMap.mapValues {
                val owner = identifierManager.getIdentifierById(it.value.contents.sub).abortOnError()
                exchangeService.getExchangedVerifiableCredential(it.value, owner, pairwiseIdentifier).abortOnError()
            }
            val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
            SdkLog.v("mPerf - Exchange Vcs (PRESENTATION}): ${loadTimeInMs}ms")
            Result.Success(exchangedVcMap as RequestedVcPresentationSubmissionMap)
        }
    }

    private suspend fun formAndSendResponse(
        response: PresentationResponse,
        responder: Identifier,
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<Unit> {
        val startLoad = System.nanoTime()
        val formattedResponse = presentationResponseFormatter.formatResponse(
            requestedVcPresentationSubmissionMap = requestedVcPresentationSubmissionMap,
            presentationResponse = response,
            responder = responder,
            expiryInSeconds = expiryInSeconds
        )
        val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - format Response for SendPresentation (PRESENTATION}): ${loadTimeInMs}ms")
        return SendPresentationResponseNetworkOperation(
            response.audience,
            formattedResponse,
            response.request.content.state,
            apiProvider
        ).fire()
    }
}