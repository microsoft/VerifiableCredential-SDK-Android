// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.di

import com.microsoft.did.sdk.backup.content.microsoft2020.TestVcMetaData
import com.microsoft.did.sdk.backup.content.microsoft2020.VcMetadata
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val defaultTestSerializer = SdkModule().defaultJsonSerializer(
    SerializersModule {
        polymorphic(VcMetadata::class) {
            subclass(TestVcMetaData::class)
        }
    }
)