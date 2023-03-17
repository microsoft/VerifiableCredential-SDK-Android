// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.microsoft.did.sdk.di.defaultTestSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.assertj.core.api.Assertions
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class PinDetailsTest {
    private val pinDetailsHashedFlowSerializedString = """{
      "length": 4,
      "type": "numeric",
      "alg": "sha256",
      "iterations": 1,
      "salt": "sakGSIfjwqroREOujhuj3j",
      "hash": "C1if7XayVIBrhnCfyCnsTiMn/myBI0XQ13vtfBGIUj8="
   }"""

    private val pinDetailsNonHashedFlowSerializedString = """{
      "length": 4,
      "type": "numeric",
      "hash": "C1if7XayVIBrhnCfyCnsTiMn/myBI0XQ13vtfBGIUj8="
   }"""

    private val pinDetailsWithNoLengthSerializedString = """{
      "type": "numeric",
      "hash": "C1if7XayVIBrhnCfyCnsTiMn/myBI0XQ13vtfBGIUj8="
   }"""

    private val pinDetailsWithNoTypeSerializedString = """{
      "length": 4,
      "hash": "C1if7XayVIBrhnCfyCnsTiMn/myBI0XQ13vtfBGIUj8="
   }"""

    private val pinDetailsWithNoHashSerializedString = """{
      "length": 4,
      "type": "numeric"
   }"""

    @Test
    fun `test serialization of pin details for hashed flow passes`() {
        val pinDetail = defaultTestSerializer.decodeFromString(PinDetails.serializer(), pinDetailsHashedFlowSerializedString)
        assertThat(pinDetail.length).isEqualTo(4)
        assertThat(pinDetail.salt).isEqualTo("sakGSIfjwqroREOujhuj3j")
        assertThat(pinDetail.type).isEqualTo("numeric")
    }

    @Test
    fun `test serialization of pin details for non hashed flow passes`() {
        val pinDetail = defaultTestSerializer.decodeFromString(PinDetails.serializer(), pinDetailsNonHashedFlowSerializedString)
        assertThat(pinDetail.length).isEqualTo(4)
        assertThat(pinDetail.type).isEqualTo("numeric")
        assertThat(pinDetail.salt).isNull()
    }

    @Test
    fun `test serialization of pin details when missing length fails`() {
        Assertions.assertThatThrownBy {
            defaultTestSerializer.decodeFromString(PinDetails.serializer(), pinDetailsWithNoLengthSerializedString)
        }.isInstanceOf(MissingFieldException::class.java)
    }

    @Test
    fun `test serialization of pin details when missing type fails`() {
        Assertions.assertThatThrownBy {
            defaultTestSerializer.decodeFromString(PinDetails.serializer(), pinDetailsWithNoTypeSerializedString)
        }.isInstanceOf(MissingFieldException::class.java)
    }

    @Test
    fun `test serialization of pin details when missing hash passes`() {
        val pinDetail = defaultTestSerializer.decodeFromString(PinDetails.serializer(), pinDetailsWithNoHashSerializedString)
        assertThat(pinDetail.type).isEqualTo("numeric")
        assertThat(pinDetail.length).isEqualTo(4)
        assertThat(pinDetail.salt).isNull()
    }
}