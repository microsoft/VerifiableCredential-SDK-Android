// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> LiveData<T>.getOrAwaitValue(time: Long = 2): T? {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> { liveData ->
        data = liveData
        latch.countDown()
    }
    this.observeForever(observer)
    latch.await(time, TimeUnit.SECONDS)
    this.removeObserver(observer)
    return data
}