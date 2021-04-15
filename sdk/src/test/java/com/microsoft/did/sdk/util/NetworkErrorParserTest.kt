// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkErrorParserTest {

    @Test
    fun `parse null`() {
        assertThat(NetworkErrorParser.extractInnerErrorsCodes(null)).isNull()
    }

    @Test
    fun `parse empty String`() {
        assertThat(NetworkErrorParser.extractInnerErrorsCodes("")).isEqualTo("")
    }

    @Test
    fun `parse invalid json`() {
        val testJson = "{ error:: code}"
        assertThat(NetworkErrorParser.extractInnerErrorsCodes(testJson)).isEqualTo("")
    }

    @Test
    fun `parse literal`() {
        val literal = "Errorcode: 241"
        assertThat(NetworkErrorParser.extractInnerErrorsCodes(literal)).isEqualTo("")
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
        val actualConcatError = NetworkErrorParser.extractInnerErrorsCodes(testJson)
        assertThat(actualConcatError).isEqualTo(expectedConcatError)
    }

    @Test
    fun `parse innererror depth 3, but code is missing in depth 2`() {
        val testJson = """
            {
              "error": {
                "code": "12345",
                "message": "Previous passwords may not be reused",
                "innererror": { "message": "message but no code :(",
                    "innererror": { "code": "abc", "message": "code won't be picked up" }
                    }
                }
            }""".trimIndent()
        val expectedConcatError = "12345"
        val actualConcatError = NetworkErrorParser.extractInnerErrorsCodes(testJson)
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
        val actualConcatError = NetworkErrorParser.extractInnerErrorsCodes(testJson)
        assertThat(actualConcatError).isEqualTo(expectedConcatError)
    }
}