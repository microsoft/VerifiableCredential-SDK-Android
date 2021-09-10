// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.internal

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.util.ImageUtil
import com.microsoft.did.sdk.util.log.SdkLog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ImageLoader @Inject constructor() {

    companion object {
        const val MAX_IMAGE_SIZE_BYTES = 1000000 // 1 MB
    }

    suspend fun loadRemoteImage(request: IssuanceRequest) {
        val logo = request.contract.display.card.logo
        if (logo != null) {
            if (logo.image != null || logo.uri == null) {
                return
            }
            logo.image = loadImageToBase64(logo.uri!!)
        }
    }

    suspend fun loadRemoteImage(request: PresentationRequest) {
        val logo = request.content.registration.logoData
        if (logo == null) {
            val logoUri = request.content.registration.logoUri
            try {
                request.content.registration.logoData = loadImageToBase64(logoUri)
            } catch (ioe: IOException) {
                SdkLog.d("Exception while loading presentation logo", ioe)
                request.content.registration.logoData = null
            }
        }
    }

    private suspend fun loadImageToBase64(uri: String): String = withContext(Dispatchers.IO) {
        val imageBitmap = Picasso.get().load(uri).get()
        // TODO: check is temporarily disabled because file size is not matching bytecount by order of magnitues sometimes
//        if (imageBitmap.byteCount > MAX_IMAGE_SIZE_BYTES)
//            throw InvalidImageException("Image size exceeds max file size ${MAX_IMAGE_SIZE_BYTES / 1000000.0f}MB")
        return@withContext ImageUtil.convert(imageBitmap)
    }
}