package com.microsoft.portableIdentity.sdk.auth.models

interface ResponseContent {

    /**
     * populates properties from request.
     */
    fun populateFromRequest(requestContent: RequestContent)
}