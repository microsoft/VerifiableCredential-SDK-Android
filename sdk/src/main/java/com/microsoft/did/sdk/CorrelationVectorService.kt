/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.content.Context
import android.preference.PreferenceManager
import com.microsoft.correlationvector.CorrelationVector
import com.microsoft.correlationvector.CorrelationVectorVersion
import com.microsoft.did.sdk.util.Constants
import javax.inject.Inject

class CorrelationVectorService @Inject constructor(private val context: Context) {

    fun createAndSave(): String {
        val correlationId = CorrelationVector(CorrelationVectorVersion.V2).value
        saveCorrelationVector(context, correlationId)
        return correlationId
    }

    fun incrementAndSave(): String {
        val correlationVectorString = getCorrelationVector(context)
        if (correlationVectorString != null) {
            val correlationVectorIncremented = CorrelationVector.parse(correlationVectorString).increment()
            saveCorrelationVector(context, correlationVectorIncremented)
            return correlationVectorIncremented
        }
        return ""
    }

    fun getCorrelationVector(applicationContext: Context = context): String? {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(Constants.CORRELATION_VECTOR_IN_PREF, null)
    }

    private fun saveCorrelationVector(applicationContext: Context, correlationId: String) {
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .putString(Constants.CORRELATION_VECTOR_IN_PREF, correlationId).apply()
    }
}