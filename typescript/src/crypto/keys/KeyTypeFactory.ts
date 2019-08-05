import CryptoHelpers from "../utilities/CryptoHelpers";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Enumeration to model key types.
 */
export enum KeyType {
  Oct = 'oct',
  EC = 'EC',
  RSA = 'RSA'
}

/**
 * Factory class to create @enum KeyType objects
 */
export default class KeyTypeFactory {
  /**
   * Create the key type according to the selected algorithm.
   * @param algorithm Web crypto compliant algorithm object
   */
  public static createViaWebCrypto (algorithm: any): KeyType {
    switch (algorithm.name.toLowerCase()) {
      case 'hmac':
        return KeyType.Oct;

      case 'ecdsa':
        return KeyType.EC;

      case 'ecdh':
        return KeyType.EC;

        case 'rsassa-pkcs1-v1_5':
        return KeyType.RSA;

      case 'rsa-oaep':
      case 'rsa-oaep-256':
        return KeyType.RSA;

      default:
        throw new Error(`The algorithm '${algorithm.name}' is not supported`);
    }
  }
  
  /**
   * Create the key use according to the selected algorithm.
   * @param algorithm JWA algorithm constant
   */
  public static createViaJwa (algorithm: string): KeyType {
    const alg = CryptoHelpers.jwaToWebCrypto(algorithm);
    return KeyTypeFactory.createViaWebCrypto(alg);
  }
}
