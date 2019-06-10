/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
// import { SubtleCrypto } from 'webcrypto-core'
import { CryptoAlgorithm } from '../keyStore/IKeyStore';
import PrivateKey from '../keys/PrivateKey';

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default interface ISubtleCrypto extends SubtleCrypto {
  /**
   * Generate a pairwise key
   * @param algorithm for the key
   * @param seedReference Reference to the seed
   * @param personaId Id for the persona
   * @param peerId Id for the peer
   * @param extractable True if key is exportable
   * @param keyops Key operations
   */
  generatePairwiseKey(algorithm: EcKeyGenParams | RsaHashedKeyGenParams, seedReference: string, personaId: string, peerId: string, extractable: boolean, keyops: string[]): Promise<PrivateKey>;  
  
  /**
   * Sign with a key referenced in the key store.
   * The referenced key must be a jwk key.
   * @param algorithm used for signature
   * @param keyReference points to key in the key store
   * @param data to sign
   */
   signByKeyStore(algorithm: CryptoAlgorithm, keyReference: string, data: BufferSource): PromiseLike<ArrayBuffer>;  

   /**
   * Verify with JWK.
   * @param algorithm used for verification
   * @param jwk Json web key used to verify
   * @param signature to verify
   * @param payload which was signed
   */
   verifyByJwk(algorithm: CryptoAlgorithm, jwk: JsonWebKey, signature: BufferSource, payload: BufferSource): Promise<boolean>;
        
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
