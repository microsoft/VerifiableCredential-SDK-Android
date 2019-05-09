/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import PublicKey from './keys/PublicKey';
import PrivateKey from './keys/PrivateKey';

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default interface ICryptoSuite {
 /**
  * Gets all of the Encrypter Algorithms from the plugin
  * @returns a dictionary with the name of the algorithm for encryption/decryption as the key
  */
  getEncrypters (): { [algorithm: string]: Encrypter };

  /**
   * Get all of the symmetric encrypter algorithms from the plugin
   * @returns a dictionary with the name of the algorithm for encryption/decryption as the key
   */
  getSymmetricEncrypters (): { [algorithm: string]: SymmetricEncrypter };

 /**
  * Gets all of the Signer Algorithms from the plugin
  * @returns a dictionary with the name of the algorithm for sign and verify as the key
  */
  getSigners (): {[algorithm: string]: Signer };
}

/**
 * Interface for Encryption/Decryption
 */
export interface Encrypter {
  // Given the data to encrypt and a JWK public key, encrypts the data
  encrypt (data: Buffer, jwk: PublicKey): Promise<Buffer>;

  // Given the encrypted data and a jwk private key, decrypts the data
  decrypt (data: Buffer, jwk: PrivateKey): Promise<Buffer>;
}

/**
 *  Interface for Signing/Signature Verification
 */
export interface Signer {
  // Given signature input content and a JWK private key, creates and returns a signature as a base64 string
  sign (content: string, jwk: PrivateKey): Promise<string>;

  /**
   * Given the content used in the original signature input, the signature, and a JWK public key,
   * returns true if the signature is valid, else false
   */
  verify (signedContent: string, signature: string, jwk: PublicKey): Promise<boolean>;
}

/**
 * Interface for symmetric encryption and decryption
 */
export interface SymmetricEncrypter {
  /**
   * Given plaintext to encrypt, and additional authenticated data, creates corresponding ciphertext and
   * provides the corresponding initialization vector, key, and tag. Note, not all
   * @param plaintext Data to be symmetrically encrypted
   * @param additionalAuthenticatedData Data that will be integrity checked but not encrypted
   * @returns An object containing the corresponding ciphertext, initializationVector, key, and tag
   */
  encrypt (plaintext: Buffer, additionalAuthenticatedData: Buffer): Promise<{ciphertext: Buffer, initializationVector: Buffer, key: Buffer, tag: Buffer}>;

  /**
   * Given the ciphertext, additional authenticated data, initialization vector, key, and tag,
   * decrypts the ciphertext.
   * @param ciphertext Data to be decrypted
   * @param additionalAuthenticatedData Integrity checked data
   * @param initializationVector Initialization vector
   * @param key Symmetric key
   * @param tag Authentication tag
   * @returns the plaintext of ciphertext
   */
  decrypt (ciphertext: Buffer, additionalAuthenticatedData: Buffer, initializationVector: Buffer, key: Buffer, tag: Buffer): Promise<Buffer>;
}
