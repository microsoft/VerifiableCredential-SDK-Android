/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import ICryptoSuite, { Encrypter, Signer, SymmetricEncrypter } from './ICryptoSuite';

// A dictionary of JWA encryption algorithm names to Encrypter objects 
type EncrypterMap = {[name: string]: Encrypter};
// A dictionary of JWA encryption algorithm names to Encyprter objects 
type SymmetricEncrypterMap = {[name: string]: SymmetricEncrypter};
// A dictionary of JWA signing algorithm names to Signer objects 
type SignerMap = { [name: string]: Signer };

/**
 * Utility class to handle all CryptoSuite dependency injection
 */
export default class CryptoFactory {

  private encrypters: EncrypterMap;
  private symmetricEncrypters: SymmetricEncrypterMap;
  private signers: SignerMap;
  private defaultSymmetricAlgorithm: string;

  /**
   * Constructs a new CryptoRegistry
   * @param suites The suites to use for dependency injection
   */
  constructor (suites: ICryptoSuite[], defaultSymmetricAlgorithm?: string) {
    this.encrypters = {};
    this.symmetricEncrypters = {};
    this.signers = {};
    this.defaultSymmetricAlgorithm = 'none';

    // takes each suite (CryptoSuite objects) and maps to name of the algorithm.
    suites.forEach((suite) => {
      const encAlgorithms = suite.getEncrypters();
// tslint:disable: no-for-in
      for (const encrypterKey in encAlgorithms) {
        this.encrypters[encrypterKey] = encAlgorithms[encrypterKey];
      }

      const symEncAlgorithms = suite.getSymmetricEncrypters();
      for (const encrypterKey in symEncAlgorithms) {
        this.symmetricEncrypters[encrypterKey] = symEncAlgorithms[encrypterKey];
      }

      const signerAlgorithms = suite.getSigners();
      for (const signerKey in signerAlgorithms) {
        this.signers[signerKey] = signerAlgorithms[signerKey];
      }
    });

    if (defaultSymmetricAlgorithm) {
      this.defaultSymmetricAlgorithm = defaultSymmetricAlgorithm;
    } else {
      for (const algorithm in this.symmetricEncrypters) {
        this.defaultSymmetricAlgorithm = algorithm;
        break;
      }
    }
  }

  /**
   * Gets the Encrypter object given the encryption algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding Encrypter, if any
   */
  getEncrypter (name: string): Encrypter {
    return this.encrypters[name];
  }

  /**
   * Gets the Signer object given the signing algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding Signer, if any
   */
  getSigner (name: string): Signer {
    return this.signers[name];
  }

  /**
   * Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding SymmetricEncrypter, if any
   */
  getSymmetricEncrypter (name: string): SymmetricEncrypter {
    return this.symmetricEncrypters[name];
  }

  /**
   * Gets the default symmetric encryption algorithm to use
   */
  getDefaultSymmetricEncryptionAlgorithm (): string {
    return this.defaultSymmetricAlgorithm;
  }
}
