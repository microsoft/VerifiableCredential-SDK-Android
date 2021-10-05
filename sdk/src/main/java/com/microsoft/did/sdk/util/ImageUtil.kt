// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.microsoft.did.sdk.util.log.SdkLog
import java.io.ByteArrayOutputStream

object ImageUtil {
    fun parse(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val decodedBytes = Base64.decode(base64Str, Constants.BASE64_URL_SAFE)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (ex: Exception) {
            SdkLog.d("Image couldn't be converted from Base64", ex)
            null
        }
    }

    fun convert(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Constants.BASE64_URL_SAFE)
    }
}