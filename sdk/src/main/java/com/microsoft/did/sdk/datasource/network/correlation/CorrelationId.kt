// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.correlation

import com.microsoft.correlationvector.CorrelationVector
import com.microsoft.correlationvector.CorrelationVectorVersion

class CorrelationId(correlationId: String? = null) {
    private var correlationVector: CorrelationVector

    init {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = false
        if (correlationId != null)
            this.correlationVector = CorrelationVector.parse(correlationId)
        else
            this.correlationVector = CorrelationVector(CorrelationVectorVersion.V2)
    }

    fun extend(correlationVector: String): String {
        this.correlationVector = CorrelationVector.extend(correlationVector)
        return this.correlationVector.value
    }

    fun increment(): String {
        this.correlationVector.increment()
        return this.correlationVector.value
    }

    fun getCorrelationId(): String {
        return this.correlationVector.value
    }
}