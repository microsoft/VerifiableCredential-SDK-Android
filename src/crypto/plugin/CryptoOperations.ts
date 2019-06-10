/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import { SubtleCrypto } from 'webcrypto-core';

/**
 * A dictionary of JWA encryption algorithm names to a crypto object
 */
export type CryptoSuiteMap = {[name: string]: CryptoOperations };

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default abstract class CryptoOperations  {
 /**
  * Gets all of the key encryption Algorithms from the plugin
  * @returns a subtle crypto object for key encryption/decryption
  */
 abstract getKeyEncrypters (): SubtleCrypto;

 /**
  * Gets all of the key sharing encryption Algorithms from the plugin
  * @returns a subtle crypto object for key sharing encryption/decryption
  */
 abstract getSharedKeyEncrypters (): SubtleCrypto;

 /**
   * Get all of the symmetric encrypter algorithms from the plugin
  * @returns a subtle crypto object for symmetric encryption/decryption
   */
  abstract getSymmetricEncrypters (): SubtleCrypto;

 /**
  * Gets all of the message signing Algorithms from the plugin
 * @returns a subtle crypto object for message signing
   */
  abstract getMessageSigners (): SubtleCrypto;

 /**
  * Gets all of the message authentication code signing Algorithms from the plugin. 
  * Will be used for primitive operations such as key generation.
 * @returns a subtle crypto object for message signing
   */
  abstract messageAuthenticationCodeSigners (): SubtleCrypto;

 /**
  * Gets all of the message digest Algorithms from the plugin. 
 * @returns a subtle crypto object for message digests
   */
  abstract getMessageDigests (): SubtleCrypto;
}
