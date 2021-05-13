// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken

class JwePasswordProtectedBackupData internal constructor(
    val jweToken: JweToken,
) : ProtectedBackupData()