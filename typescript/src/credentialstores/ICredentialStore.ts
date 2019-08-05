/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Interface defining methods and properties to
 * be implemented by specific credential stores.
 */
export default interface ICredentialStore {
  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyIdentifier for which to return the key.
   */
  write (): Promise<void>;

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  read (): Promise<any>;
}
