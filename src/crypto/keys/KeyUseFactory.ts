import CryptoHelpers from "../utilities/CryptoHelpers";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Enumeration to model key use.
 */
export enum KeyUse {
  Encryption = 'enc',
  Signature = 'sig'
}

/**
 * Factory class to create @enum KeyUse objects.
 */
export default class KeyUseFactory {
  /**
   * Create the key use according to the selected algorithm.
   * @param algorithm Web crypto compliant algorithm object
   */
  public static createViaWebCrypto (algorithm: any): KeyUse {
    switch (algorithm.name.toLowerCase()) {
      case 'hmac':
        return KeyUse.Signature;

      case 'ecdsa':
        return KeyUse.Signature;

      case 'ecdh':
        return KeyUse.Encryption;

      case 'rsassa-pkcs1-v1_5':
        return KeyUse.Signature;

      default:
        throw new Error(`The algorithm '${algorithm.name}' is not supported`);
    }
  }
  
  /**
   * Create the key use according to the selected algorithm.
   * @param algorithm JWA algorithm constant
   */
  public static createViaJwa (algorithm: string): KeyUse {
    const alg = CryptoHelpers.jwaToWebCrypto(algorithm);
    return KeyUseFactory.createViaWebCrypto(alg);
  }
}
