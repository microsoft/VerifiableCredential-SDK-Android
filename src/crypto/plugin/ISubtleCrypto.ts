/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { SubtleCrypto } from 'webcrypto-core'
import { CryptoAlgorithm } from '../keyStore/IKeyStore';

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default interface ISubtleCrypto extends SubtleCrypto {
  /**
   * Sign with a key referenced in the key store.
   * The referenced key must be a jwk key.
   * @param algorithm used for signature
   * @param keyReference points to key in the key store
   * @param data to sign
   */
   signByKeyStore(algorithm: CryptoAlgorithm, keyReference: string, data: BufferSource): PromiseLike<ArrayBuffer>;  
        
  /**
   * Decrypt with a key referenced in the key store.
   * The referenced key must be a jwk key.
   * @param algorithm used for encryption
   * @param keyReference points to key in the key store
   * @param cipher to decrypt
   */
   decryptByKeyStore(algorithm: CryptoAlgorithm, keyReference: string, cipher: BufferSource): PromiseLike<ArrayBuffer>;  
  
   /**
   * Decrypt with JWK.
   * @param algorithm used for decryption
   * @param jwk Json web key to decrypt
   * @param cipher to decrypt
   */
   decryptByJwk(algorithm: CryptoAlgorithm, jwk: JsonWebKey, cipher: BufferSource): Promise<ArrayBuffer>;

   /**
   * Encrypt with a jwk key referenced in the key store
   * @param algorithm used for encryption
   * @param keyReference points to key in the key store
   * @param data to sign
   */
  encryptByJwk(algorithm: CryptoAlgorithm, key: JsonWebKey, data: BufferSource): Promise<ArrayBuffer>;
}
