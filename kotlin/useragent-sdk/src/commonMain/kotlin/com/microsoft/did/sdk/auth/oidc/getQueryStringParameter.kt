package com.microsoft.did.sdk.auth.oidc

import com.microsoft.did.sdk.auth.OAuthRequestParameter
import com.microsoft.did.sdk.utilities.ILogger
import com.microsoft.did.sdk.utilities.PercentEncoding


fun getQueryStringParameter(name: OAuthRequestParameter, url: String, required: Boolean = false, logger: ILogger): String? {
    val findResults = Regex("${name.value}=([^&]+)").find(url)
    if (findResults != null) {
        return PercentEncoding.decode(findResults.groupValues[1], logger = logger)
    } else if (required) {
        throw logger.error("Openid requires a \"${name.value}\" parameter")
    }
    return null
}