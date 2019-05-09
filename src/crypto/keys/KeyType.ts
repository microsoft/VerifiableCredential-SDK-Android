/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * enum to model key types
 */
export enum KeyType {
  Oct = 'oct',
  EC = 'EC',
  RSA = 'RSA'
}

/**
 * Factory class to create KeyType objects
 */
export default class KeyTypeFactory {
  /**
   * Create the key type according to the selected algortihm.
   * @param algorithm Web crypto compliant algorithm object
   */
  public static create (algorithm: any): KeyType {
    switch (algorithm.name.toLowerCase()) {
      case 'hmac':
        return KeyType.Oct;

      case 'ecdsa':
        return KeyType.EC;

      case 'ecdh':
        return KeyType.EC;

      case 'rsassa-pkcs1-v1_5':
        return KeyType.RSA;

      default:
        throw new Error(`The algorithm '${algorithm.name}' is not supported`);
    }
  }
}
