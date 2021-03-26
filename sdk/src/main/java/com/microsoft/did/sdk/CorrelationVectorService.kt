/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.content.SharedPreferences
import com.microsoft.correlationvector.CorrelationVector
import com.microsoft.correlationvector.CorrelationVectorVersion
import com.microsoft.did.sdk.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CorrelationVectorService @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun startNewFlowAndSave(): String {
        val correlationId = CorrelationVector(CorrelationVectorVersion.V2).value
        saveCorrelationVector(sharedPreferences, correlationId)
        return correlationId
    }

    fun incrementAndSave(): String {
        val correlationVectorString = getCorrelationVector(sharedPreferences)
        if (correlationVectorString != null && correlationVectorString.isNotEmpty()) {
            val correlationVectorIncremented = CorrelationVector.parse(correlationVectorString).increment()
            saveCorrelationVector(sharedPreferences, correlationVectorIncremented)
            return correlationVectorIncremented
        }
        return ""
    }

    fun getCorrelationVector(sharedPreference: SharedPreferences = sharedPreferences): String? {
        return sharedPreference.getString(Constants.CORRELATION_VECTOR_IN_PREF, null)
    }

    private fun saveCorrelationVector(sharedPreferences: SharedPreferences, correlationId: String) {
        if (correlationId.isNotEmpty())
            sharedPreferences.edit()
                .putString(Constants.CORRELATION_VECTOR_IN_PREF, correlationId).apply()
    }
}