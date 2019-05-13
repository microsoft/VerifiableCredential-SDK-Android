/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoSuite from './CryptoSuite';
import ISubtleCrypto from './ISubtleCrypto'
import { SubtleCrypto } from 'webcrypto-core';

/**
 * Default crypto suite
 *  */
export default class DefaultCryptoSuite extends CryptoSuite {
 /**
  * Gets all of the key encryption Algorithms from the plugin
  * @returns a subtle crypto object for key encryption/decryption
  */
 public getKekEncrypters (): ISubtleCrypto {
  if (window.crypto.subtle) {
    // return browser api
    return window.crypto.subtle as ISubtleCrypto;
  } else {
    // return nodejs api
    return new SubtleCrypto() as ISubtleCrypto;
  }
 }

 /**
  * Gets all of the key sharing encryption Algorithms from the plugin
  * @returns a subtle crypto object for key sharing encryption/decryption
  */
 public getSharedKeyEncrypters (): ISubtleCrypto {
  if (window.crypto.subtle) {
    // return browser api
    return window.crypto.subtle as ISubtleCrypto;
  } else {
    // return nodejs api
    return new SubtleCrypto() as ISubtleCrypto;
  }
 }

 /**
   * Get all of the symmetric encrypter algorithms from the plugin
  * @returns a subtle crypto object for symmetric encryption/decryption
   */
  public getSymmetricEncrypters (): ISubtleCrypto {
    if (window.crypto.subtle) {
      // return browser api
      return window.crypto.subtle as ISubtleCrypto;
    } else {
      // return nodejs api
      return new SubtleCrypto() as ISubtleCrypto;
    }
   }

 /**
  * Gets all of the message signing Algorithms from the plugin
 * @returns a subtle crypto object for message signing
   */
  public getMessageSigners (): ISubtleCrypto {
    if (window.crypto.subtle) {
      // return browser api
      return window.crypto.subtle as ISubtleCrypto;
    } else {
      // return nodejs api
      return new SubtleCrypto() as ISubtleCrypto;
    }
   }

 /**
  * Gets all of the MAC signing Algorithms from the plugin. 
  * Will be used for primitive operations such as key generation.
 * @returns a subtle crypto object for message signing
   */
  public getMacSigners (): ISubtleCrypto {
    if (window.crypto.subtle) {
      // return browser api
      return window.crypto.subtle as ISubtleCrypto;
    } else {
      // return nodejs api
      return new SubtleCrypto() as ISubtleCrypto;
    }
   }

 /**
  * Gets all of the message digest Algorithms from the plugin. 
 * @returns a subtle crypto object for message digests
   */
  public getMessageDigests (): ISubtleCrypto {
    if (window.crypto.subtle) {
      // return browser api
      return window.crypto.subtle as ISubtleCrypto;
    } else {
      // return nodejs api
      return new SubtleCrypto() as ISubtleCrypto;
    }
   }
}
