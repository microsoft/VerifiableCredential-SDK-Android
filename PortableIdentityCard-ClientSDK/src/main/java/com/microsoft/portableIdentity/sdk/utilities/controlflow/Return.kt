/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.utilities.controlflow

sealed class Return<S, F> {
    class Success<S, F>(val payload: S) : Return<S, F>()
    class Failure<S, F>(val payload: F) : Return<S, F>()
}
