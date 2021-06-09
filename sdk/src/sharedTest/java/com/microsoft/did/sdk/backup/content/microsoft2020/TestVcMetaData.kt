// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TestVcMetaData")
data class TestVcMetaData(override val displayContract: DisplayContract) : VcMetadata()