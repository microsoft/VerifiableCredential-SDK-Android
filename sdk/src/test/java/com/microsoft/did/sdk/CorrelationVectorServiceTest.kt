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
        val actualIndex = Integer.valueOf(createdCorrelationVector.substring(createdCorrelationVector.lastIndexOf(".") + 1))
        assertThat(actualIndex).isEqualTo(0)
    }

    @Test
    fun `test increment and save correlation vector`() {
        justRun { correlationVectorService["saveCorrelationVector"](mockedSharedPreferences, any<String>()) }
        every { mockedSharedPreferences.getString(Constants.CORRELATION_VECTOR_IN_PREF, null) } returns expectedCorrelationVector
        val incrementedCorrelationVector = correlationVectorService.incrementAndSave()
        val incrementedIndex = Integer.valueOf(incrementedCorrelationVector.substring(incrementedCorrelationVector.lastIndexOf(".") + 1))
        val actualIndex = Integer.valueOf(expectedCorrelationVector.substring(expectedCorrelationVector.lastIndexOf(".") + 1))
        assertThat(incrementedIndex-actualIndex).isEqualTo(1)
    }
}
