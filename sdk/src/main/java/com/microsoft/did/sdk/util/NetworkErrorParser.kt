// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.util.log.SdkLog
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object NetworkErrorParser {

    /**
     * Attempts to parse a json errorBody and extracts and concatenates all error codes.
     *
     * @param errorBody A json object. The topmost error under the propertyname "error".
     *          All subsequent errors within the error under "innererror". Each error has to contain
     *          a "code" property for parsing to continue.
     * @return all concatenated error codes delimited by "," null if errorBody is null,
     *          or empty if not a valid json
     */
    fun extractInnerErrorsCodes(errorBody: String?): String? {
        if (errorBody == null) return null
        val errorCodes = ArrayList<String>()
        try {
            val json = Json.decodeFromString<JsonObject>(errorBody)
            var error = (json["error"] as? JsonObject)
            var code = (error?.get("code") as? JsonPrimitive)?.content
            while (code != null) {
                errorCodes.add(code)
                error = (error?.get("innererror") as? JsonObject)
                code = (error?.get("code") as? JsonPrimitive)?.content
            }
        } catch (ex: Exception) {
            SdkLog.d("Parsing error response canceled. Json: $errorBody", ex)
        }
        return errorCodes.joinToString(",")
    }

    /**
     * Attempts to parse a json errorBody and extracts and concatenates all error messages.
     *
     * @param errorBody A json object. The topmost error under the propertyname "error".
     *          All subsequent errors within the error under "innererror". Each error has to contain
     *          a "message" property for parsing to continue.
     * @return top level of error message. null if errorBody is null
     *          or not a valid json
     */
    fun extractErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null
        val errorMessages = ArrayList<String>()
        try {
            val json = Json.decodeFromString<JsonObject>(errorBody)
            var error = (json["error"] as? JsonObject)
            var msg = (error?.get("message") as? JsonPrimitive)?.content
            while (msg != null) {
                errorMessages.add(msg)
                error = (error?.get("innererror") as? JsonObject)
                msg = (error?.get("message") as? JsonPrimitive)?.content
            }
        } catch (ex: Exception) {
            SdkLog.d("Parsing error response canceled. Json: $errorBody", ex)
        }
        return errorMessages.joinToString("; ")
    }
}