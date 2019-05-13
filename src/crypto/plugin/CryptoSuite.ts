/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import ISubtleCrypto from './ISubtleCrypto'

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default abstract class CryptoSuite {
 /**
  * Gets all of the key encryption Algorithms from the plugin
  * @returns a subtle crypto object for key encryption/decryption
  */
 abstract getKekEncrypters (): ISubtleCrypto;

 /**
  * Gets all of the key sharing encryption Algorithms from the plugin
  * @returns a subtle crypto object for key sharing encryption/decryption
  */
 abstract getSharedKeyEncrypters (): ISubtleCrypto;

 /**
   * Get all of the symmetric encrypter algorithms from the plugin
  * @returns a subtle crypto object for symmetric encryption/decryption
   */
  abstract getSymmetricEncrypters (): ISubtleCrypto;

 /**
  * Gets all of the message signing Algorithms from the plugin
 * @returns a subtle crypto object for message signing
   */
  abstract getMessageSigners (): ISubtleCrypto;

 /**
  * Gets all of the MAC signing Algorithms from the plugin. 
  * Will be used for primitive operations such as key generation.
 * @returns a subtle crypto object for message signing
   */
  abstract getMacSigners (): ISubtleCrypto;

 /**
  * Gets all of the message digest Algorithms from the plugin. 
 * @returns a subtle crypto object for message digests
   */
  abstract getMessageDigests (): ISubtleCrypto;
}
