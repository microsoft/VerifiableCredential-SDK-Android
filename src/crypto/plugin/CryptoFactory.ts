/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoSuite from './CryptoSuite';
import ISubtleCrypto from './ISubtleCrypto';
import DefaultCryptoSuite from './DefaultCryptoSuite';

// A dictionary of JWA encryption algorithm names to key encrypter objects 
type KeyEncrypterMap = {[name: string]: ISubtleCrypto};
// A dictionary of JWA encryption algorithm names to shared key encrypter objects 
type SharedKeyEncrypterMap = {[name: string]: ISubtleCrypto};
// A dictionary of JWA encryption algorithm names to symmetric encrypter objects 
type SymmetricEncrypterMap = {[name: string]: ISubtleCrypto};
// A dictionary of JWA signing algorithm names to message signer objects 
type MessageSignerMap = { [name: string]: ISubtleCrypto };
// A dictionary of JWA signing algorithm names to MAC signer objects 
type MacSignerMap = { [name: string]: ISubtleCrypto };
// A dictionary of JWA signing algorithm names to message digest objects 
type MessageDigestMap = { [name: string]: ISubtleCrypto };

/**
 * Utility class to handle all CryptoSuite dependency injection
 */
export default class CryptoFactory {

  private keyEncrypters: KeyEncrypterMap;
  private sharedKeyEncrypters: SharedKeyEncrypterMap;
  private symmetricEncrypter: SymmetricEncrypterMap;
  private messageSigners: MessageSignerMap;
  private macSigners: MacSignerMap;
  private messageDigests: MessageDigestMap;

  /**
   * Constructs a new CryptoRegistry
   * @param suite The suite to use for dependency injection
   */
  constructor (suite?: CryptoSuite) {
    let crypto: CryptoSuite; 
    if (suite) {
      crypto = suite;
    } else {
    // Set default API
    crypto = new DefaultCryptoSuite();
    }
    this.keyEncrypters = {'*': crypto } as KeyEncrypterMap;
    this.sharedKeyEncrypters = {'*': crypto } as SharedKeyEncrypterMap;
    this.symmetricEncrypter = {'*': crypto } as SymmetricEncrypterMap;
    this.messageSigners = {'*': crypto } as MessageSignerMap;
    this.macSigners = {'*': crypto } as MacSignerMap;
    this.messageDigests = {'*': crypto } as MessageDigestMap;
  }

  /**
   * Gets the Encrypter object given the encryption algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  public getKeyEncrypter (name: string): ISubtleCrypto {
    if (this.keyEncrypters[name]) {
      return this.keyEncrypters[name];
    }
    return this.keyEncrypters['*'];
  }

  /**
   * Gets the shared key encrypter object given the encryption algorithm's name
   * Used for DH algorithms
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  getSharedKeyEncrypter (name: string): ISubtleCrypto {
    if (this.sharedKeyEncrypters[name]) {
      return this.sharedKeyEncrypters[name];
    }
    return this.sharedKeyEncrypters['*'];
  }

  /**
   * Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  getSymmetricEncrypter (name: string): ISubtleCrypto {
    if (this.symmetricEncrypter[name]) {
      return this.symmetricEncrypter[name];
    }
    return this.symmetricEncrypter['*'];
  }
  
  /**
   * Gets the message signer object given the signing algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  getMessageSigner (name: string): ISubtleCrypto {
    if (this.messageSigners[name]) {
      return this.messageSigners[name];
    }
    return this.messageSigners['*'];
  }

  /**
   * Gets the mac signer object given the signing algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  getMacSigner (name: string): ISubtleCrypto {
    if (this.macSigners[name]) {
      return this.macSigners[name];
    }
    return this.macSigners['*'];
  }
  /**
   * Gets the message digest object given the digest algorithm's name
   * @param name The name of the algorithm
   * @returns The corresponding crypto API
   */
  getMessageDigest (name: string): ISubtleCrypto {
    if (this.messageDigests[name]) {
      return this.messageDigests[name];
    }
    return this.messageDigests['*'];
  }
}
