// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.content.SharedPreferences
import com.microsoft.did.sdk.util.Constants
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CorrelationVectorServiceTest {
    private val mockedSharedPreferences = mockk<SharedPreferences>()
    private val correlationVectorService: CorrelationVectorService = spyk(CorrelationVectorService(mockedSharedPreferences))
    private val expectedCorrelationVector = "nAPm7lyVuK16LC3o.0"

    @Test
    fun `test start new flow and save correlation vector `() {
        justRun { correlationVectorService["saveCorrelationVector"](mockedSharedPreferences, any<String>()) }
        val createdCorrelationVector = correlationVectorService.startNewFlowAndSave()
        assertThat(createdCorrelationVector.isEmpty()).isFalse
        assertThat(createdCorrelationVector.endsWith(".0")).isTrue
    }

    @Test
    fun testNullCvIncrementAndSave() {
        every { mockedSharedPreferences.getString(Constants.CORRELATION_VECTOR_IN_PREF, null) } returns null
        val incrementedCorrelationVector = correlationVectorService.incrementAndSave()
        assertThat(incrementedCorrelationVector.isEmpty()).isTrue
    }

    @Test
    fun `test increment and save correlation vector`() {
        justRun { correlationVectorService["saveCorrelationVector"](mockedSharedPreferences, any<String>()) }
        every { mockedSharedPreferences.getString(Constants.CORRELATION_VECTOR_IN_PREF, null) } returns expectedCorrelationVector
        val incrementedCorrelationVector = correlationVectorService.incrementAndSave()
        assertThat(incrementedCorrelationVector.isEmpty()).isFalse
        assertThat(incrementedCorrelationVector.endsWith(".1")).isTrue
    }
}
