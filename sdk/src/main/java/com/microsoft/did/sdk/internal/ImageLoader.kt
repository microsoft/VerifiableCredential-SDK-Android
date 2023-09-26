// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.internal

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.util.ImageUtil
import com.microsoft.did.sdk.util.controlflow.InvalidImageException
import com.microsoft.did.sdk.util.log.SdkLog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageLoader @Inject constructor() {

    companion object {
        const val MAX_IMAGE_SIZE_BYTES = 1000000 // 1 MB
        const val BASE64_SIZE_INCREASE_ESTIMATION = 1.3 // Base64 string is about 30% bigger
    }

    suspend fun loadRemoteImage(request: IssuanceRequest) {
        val logo = request.contract.display.card.logo
        if (logo != null) {
            if (logo.image != null || logo.uri == null) {
                return
            }
            logo.image = logo.uri?.let { loadImageToBase64(it) }
        }
        logo?.image?.let {
            if (it.length * 2 > MAX_IMAGE_SIZE_BYTES * BASE64_SIZE_INCREASE_ESTIMATION)
                throw InvalidImageException("Image size exceeds max file size ${MAX_IMAGE_SIZE_BYTES / 1000000.0f}MB")
        }
    }

    suspend fun loadRemoteImage(request: PresentationRequest) {
        val logo = request.content.registration.logoData
        if (logo == null) {
            val logoUri = request.content.registration.logoUri
            request.content.registration.logoData = loadImageToBase64(logoUri)
        }
        request.content.registration.logoData?.let {
            if (it.length * 2 > MAX_IMAGE_SIZE_BYTES * BASE64_SIZE_INCREASE_ESTIMATION)
                throw InvalidImageException("Image size exceeds max file size ${MAX_IMAGE_SIZE_BYTES / 1000000.0f}MB")
        }
    }

    suspend fun loadImageToBase64(uri: String): String? = withContext(Dispatchers.IO) {
        try {
            val nonEmptyUri = uri.ifBlank { null }
            val imageBitmap = Picasso.get().load(nonEmptyUri).get()

            return@withContext ImageUtil.convert(imageBitmap)
        } catch (ex: Exception) {
            SdkLog.i("Exception while loading image", ex)
            null
        }
    }
}