// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.util.ClaimFormatter
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class VerifiableCredentialHolderTest {

    private val claimDescriptor1 = ClaimDescriptor(ClaimFormatter.ClaimType.TEXT.name, "name 1", null)
    private val claimDescriptor2 = ClaimDescriptor(ClaimFormatter.ClaimType.DATE.name, "name 2", null)
    private val claimDescriptor3 = ClaimDescriptor(ClaimFormatter.ClaimType.DATE.name, "name 3", null)
    private val claimDescriptor4 = ClaimDescriptor(ClaimFormatter.ClaimType.DATE.name, "not matching name", null)
    private val vc: VerifiableCredential = mockk()
    private val displayContract: DisplayContract = mockk()
    private val vch: VerifiableCredentialHolder = spyk(VerifiableCredentialHolder("", vc, mockk(), displayContract))

    @Test
    fun `map is matched, sorted and formatted properly`() {
        every { displayContract.claims } returns mapOf(
            "vc.credentialSubject.claim1" to claimDescriptor1,
            "vc.credentialSubject.claim2" to claimDescriptor2,
            "vc.credentialSubject.claim3" to claimDescriptor3,
            "claim2" to claimDescriptor4,
            "no matching claim" to claimDescriptor4
        )
        every { vc.contents.vc.credentialSubject } returns mapOf(
            "claim2" to "value2",
            "claim1" to "value1",
            "claim3" to "1588655105000",
            "noMatchingDisplayContract" to "value 4"
        )
        val expectedResult = mapOf(
            "name 2" to "?",
            "name 1" to "value1",
            "name 3" to ClaimFormatter.formatDate(1588655105000),
            "? - noMatchingDisplayContract" to "value 4"
        )
        val actualResult = vch.getUserFormattedClaimMap()
        assertThat(actualResult).isEqualTo(expectedResult)
    }
}