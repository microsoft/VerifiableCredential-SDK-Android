package com.microsoft.portableIdentity.sdk.auth.deprecated.oidc

import com.microsoft.portableIdentity.sdk.utilities.PercentEncoding
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

fun getQueryStringParameter(name: OAuthRequestParameter, url: String, required: Boolean = false): String? {
    val findResults = Regex("${name.value}=([^&]+)").find(url)
    if (findResults != null) {
        return PercentEncoding.decode(findResults.groupValues[1])
    } else if (required) {
        throw SdkLog.error("Openid requires a \"${name.value}\" parameter")
    }
    return null
}