// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.util.controlflow.IoFailure
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.OutputStream

abstract class JweProtectedBackup {
    abstract val jweToken: JweToken
}