// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClaimFormatterTest {

    private val suppliedClaimValueForText = "claim value"
    private val expectedClaimValueForIncorrectDate = "?"
    private val suppliedClaimValueForDateInMills = 1621366869159L
    private val expectedFormattedDateTimeInDifferentTimeZone = listOf("May 18, 2021, 12:41:09 PM", "May 18, 2021, 7:41:09 PM", "May 18, 2021, 9:41:09 PM", "May 18, 2021, 3:41:09 PM")
    private val suppliedClaimValueForDateInSeconds = 1621366850L
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
        val actualFormattedClaim = ClaimFormatter.formatClaimValue("date", suppliedClaimValueForDateInSeconds.toString())
        assertThat(actualFormattedClaim).isEqualTo(expectedFormattedDate)
    }

    @Test
    fun `test formatting date successfully`() {
        val actualFormattedClaim = ClaimFormatter.formatDateInSeconds(suppliedClaimValueForDateInSeconds)
        assertThat(actualFormattedClaim).isEqualTo(expectedFormattedDate)
    }

    @Test
    fun `test formatting date and time successfully`() {
        // CLDR 42 (bundled with JDK 20+) uses a narrow no-break space (U+202F) before AM/PM; older
        // CLDR used a regular space. Normalize NBSP/NNBSP so the output still matches the expected
        // regular-space variants when tests run on JDK 21.
        val actualFormattedClaim = ClaimFormatter.formatDateAndTimeInMillis(suppliedClaimValueForDateInMills)
            .replace('\u202F', ' ')
            .replace('\u00A0', ' ')
        assertThat(actualFormattedClaim).isIn(expectedFormattedDateTimeInDifferentTimeZone)
    }
}