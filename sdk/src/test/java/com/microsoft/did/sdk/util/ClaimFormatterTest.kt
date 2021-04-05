// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClaimFormatterTest {

    private val suppliedClaimValueForText = "claim value"
    private val expectedClaimValueForIncorrectDate = "?"
    private val suppliedClaimValueForDate = 1617906551L
    private val expectedFormattedDateTimeInDifferentTimeZone = listOf("Apr 8, 2021 11:29:11 AM", "Apr 8, 2021 6:29:11 PM")
    private val expectedFormattedDate = "April 8, 2021"

    @Test
    fun `test formatting claim value with incorrect claim type successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("incorrect", suppliedClaimValueForText)
        assertThat(actualFormattedClaim).isEqualTo(suppliedClaimValueForText)
    }

    @Test
    fun `test formatting claim value with text claim type successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("text", suppliedClaimValueForText)
        assertThat(actualFormattedClaim).isEqualTo(suppliedClaimValueForText)
    }

    @Test
    fun `test formatting claim value with incorrect claim value for type date fails`() {
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("date", suppliedClaimValueForText)
        assertThat(actualFormattedClaim).isEqualTo(expectedClaimValueForIncorrectDate)
    }

    @Test
    fun `test formatting claim value with correct claim value for type date successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("date", suppliedClaimValueForDate.toString())
        assertThat(actualFormattedClaim).isIn(expectedFormattedDateTimeInDifferentTimeZone)
    }

    @Test
    fun `test formatting date successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatDate(suppliedClaimValueForDate)
        assertThat(actualFormattedClaim).isEqualTo(expectedFormattedDate)
    }
}