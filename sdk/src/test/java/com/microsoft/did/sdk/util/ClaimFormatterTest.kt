// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClaimFormatterTest {

    private val suppliedClaimValueForText = "claim value"
    private val expectedClaimValueForIncorrectDate = "?"
    private val suppliedClaimValueForDateTime = 1621366869159L
    private val expectedFormattedDateTimeInDifferentTimeZone = listOf("May 18, 2021 12:41:09 PM", "May 18, 2021 7:41:09 PM")
    private val suppliedClaimValueForDate = 1621366850L
    private val expectedFormattedDate = "May 18, 2021"

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
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("date", suppliedClaimValueForDateTime.toString())
        assertThat(actualFormattedClaim).isIn(expectedFormattedDateTimeInDifferentTimeZone)
    }

    @Test
    fun `test formatting date successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatDate(suppliedClaimValueForDate)
        assertThat(actualFormattedClaim).isEqualTo(expectedFormattedDate)
    }
}