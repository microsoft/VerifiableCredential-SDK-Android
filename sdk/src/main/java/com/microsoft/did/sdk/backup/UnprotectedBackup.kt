// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup

/**
 * Backup classes represent the same Backups but imported into the class model of the SDK.
 * On the other hand, BackupData classes represent the data as found in the backup file.
 *
 * This class serves as the parent class for any kind of backup that may hold any kind of data.
 * This way the implementations of backups can evolve without the APIs changing.
 */
abstract class UnprotectedBackup