/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

/**
 * Type of Protocol of a Request/Response.
 * We could support some other protocol for interop in the future.
 * However, we only support SIOP for now.
 */
enum class ProtocolType {
    SIOP
}