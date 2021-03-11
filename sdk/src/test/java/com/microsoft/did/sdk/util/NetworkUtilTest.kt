// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkUtilTest {

    @Test
    fun `parse null`() {
        assertThat(NetworkUtil.parseInnerErrors(null)).isNull()
    }

    @Test
    fun `parse empty String`() {
        assertThat(NetworkUtil.parseInnerErrors("")).isEqualTo("")
    }

    @Test
    fun `parse invalid json`() {
        val testJson = "{ error:: code}"
        assertThat(NetworkUtil.parseInnerErrors(testJson)).isEqualTo("")
    }

    @Test
    fun `parse literal`() {
        val literal = "Errorcode: 241"
        assertThat(NetworkUtil.parseInnerErrors(literal)).isEqualTo("")
    }

    @Test
    fun `parse innererror depth 1`() {
        val testJson = """
            {
              "error": {
                "code": "12345",
                "message": "Previous passwords may not be reused"
                }
            }""".trimIndent()
        val expectedConcatError = "12345"
        val actualConcatError = NetworkUtil.parseInnerErrors(testJson)
        assertThat(actualConcatError).isEqualTo(expectedConcatError)
    }

    @Test
    fun `parse innererror depth 1,5`() {
        val testJson = """
            {
              "error": {
                "code": "12345",
                "message": "Previous passwords may not be reused",
                "innererror": { "message": "message but no code :(" }
                }
            }""".trimIndent()
        val expectedConcatError = "12345"
        val actualConcatError = NetworkUtil.parseInnerErrors(testJson)
        assertThat(actualConcatError).isEqualTo(expectedConcatError)
    }

    @Test
    fun `parse innererror depth 4`() {
        val testJson = """
            {
              "error": {
                "code": "BadArgument",
                "message": "Previous passwords may not be reused",
                "target": "password",
                "innererror": {
                  "code": "PasswordError",
                  "innererror": {
                    "code": "PasswordDoesNotMeetPolicy",
                    "minLength": "6",
                    "maxLength": "64",
                    "characterTypes": ["lowerCase","upperCase","number","symbol"],
                    "minDistinctCharacterTypes": "2",
                    "innererror": {
                      "code": "PasswordReuseNotAllowed"
                    }
                  }
                }
              }
            }
        """.trimIndent()
        val expectedConcatError = "BadArgument,PasswordError,PasswordDoesNotMeetPolicy,PasswordReuseNotAllowed"
        val actualConcatError = NetworkUtil.parseInnerErrors(testJson)
        assertThat(actualConcatError).isEqualTo(expectedConcatError)
    }
}