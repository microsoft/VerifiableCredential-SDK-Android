// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
abstract class UnprotectedBackup {
    abstract val type: String
    companion object {
        val serializer =  Json { serializersModule = SerializersModule {
                polymorphic(UnprotectedBackup::class) {
                    subclass(MicrosoftUnprotectedBackup2020::class)
                }
            }
        }
    }
    fun toString(jsonSerializer: Json): String {
        return jsonSerializer.encodeToString(this)
    }

    abstract suspend fun import(): Result<Unit>
}
