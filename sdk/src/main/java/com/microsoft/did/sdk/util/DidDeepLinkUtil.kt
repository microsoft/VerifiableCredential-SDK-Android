// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import android.net.Uri

object DidDeepLinkUtil {

    private const val DEEP_LINK_SCHEME = "openid"
    private const val DEEP_LINK_HOST = "vc"

    private const val DEEP_LINK_SCHEME2 = "openid-vc"

    fun isDidDeepLink(url: Uri): Boolean {
        return isCustomSchema1(url) || isCustomSchema2(url)
    }

    private fun isCustomSchema1(url: Uri): Boolean {
        return url.scheme == DEEP_LINK_SCHEME
            && url.host == DEEP_LINK_HOST
    }

    private fun isCustomSchema2(url: Uri): Boolean {
        return url.scheme == DEEP_LINK_SCHEME2
    }
}