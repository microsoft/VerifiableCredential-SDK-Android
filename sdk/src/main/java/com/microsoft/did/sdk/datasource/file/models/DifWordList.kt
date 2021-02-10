// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import android.content.Context
import android.content.res.AssetManager
import java.io.InputStreamReader

object DifWordList {
    lateinit var wordList: List<String>

    internal fun initializeWordList(context: Context) {
        this.wordList = InputStreamReader(context.assets.open("difwordlist.txt", AssetManager.ACCESS_STREAMING), "UTF-8").readLines()
    }
}
