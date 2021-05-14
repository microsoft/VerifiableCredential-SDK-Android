// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content

/**
 * A ProtectedBackup holds a UnprotectedBackupData in some shape or form.
 * The details are defined by implementations of this class.
 * e.g. a JWE Token encrypted by a password.
 */
abstract class ProtectedBackupData {
    abstract fun serialize(): String
}