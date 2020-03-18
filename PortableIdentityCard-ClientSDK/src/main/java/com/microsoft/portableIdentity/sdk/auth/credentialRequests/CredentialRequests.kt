/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.credentialRequests

import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object that bundles types of credential requests together if exists.
 */
@Serializable
open class CredentialRequests(
    override val entries: Set<Map.Entry<String, InputClaim>>,
    override val keys: Set<String>,
    override val size: Int,
    override val values: Collection<InputClaim>
): Map<String, InputClaim> {

    override fun containsKey(key: String): Boolean {
        return keys.contains(key)
    }

    override fun containsValue(value: InputClaim): Boolean {
        entries.forEach {
            if (value == it.value) {
                return true
            }
        }
        return false
    }

    override fun get(key: String): InputClaim? {
        entries.forEach {
            if (key == it.key) {
                return it.value
            }
        }
        return null
    }

    override fun isEmpty(): Boolean {
        if (size == 0) {
            return true
        }
        return false
    }

}

@Polymorphic
@SerialName("IdToken")
class IdTokenRequests(override val entries: Set<Map.Entry<String, InputClaim>>,
                      override val keys: Set<String>,
                      override val size: Int,
                      override val values: Collection<InputClaim>
) : CredentialRequests(entries, keys, size, values)
//
//@Serializable
//@SerialName("VerifiableCredential")
//class VerifiableCredentialRequests(override val entries: Set<Map.Entry<String, InputClaim>>,
//                                   override val keys: Set<String>,
//                                   override val size: Int,
//                                   override val values: Collection<InputClaim>) : CredentialRequests()
//
//@Serializable
//@SerialName("SelfIssued")
//class SelfIssuedCredentialRequests(override val entries: Set<Map.Entry<String, InputClaim>>,
//                                   override val keys: Set<String>,
//                                   override val size: Int,
//                                   override val values: Collection<InputClaim>) : CredentialRequests()

/**
 * A data object to represent optional properties in a claim request.
 */
@Serializable
data class InputClaim(
    val essential: Boolean?,
    val purpose: String?)