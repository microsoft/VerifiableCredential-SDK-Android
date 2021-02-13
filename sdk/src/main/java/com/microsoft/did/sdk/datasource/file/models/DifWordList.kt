// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import android.content.Context
import android.content.res.AssetManager
import java.io.InputStreamReader

object DifWordList {
    internal fun getWordList(context: Context): List<String> {
        val reader = InputStreamReader(context.assets.open("difwordlist.txt", AssetManager.ACCESS_STREAMING), "UTF-8")
        val words = reader.readLines()
        reader.close()
        return words
    }
}
