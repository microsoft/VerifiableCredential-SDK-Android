/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import ISubtleCrypto from './ISubtleCrypto'
import IKeyStore, { CryptoAlgorithm } from '../keystore/IKeyStore';
import CryptoFactory from './CryptoFactory';
import CryptoHelpers from '../utilities/CryptoHelpers';
import PublicKey from '../keys/PublicKey';
import { KeyType } from '../keys/KeyType';
import { SubtleCrypto } from 'webcrypto-core';

/**
 * Default crypto suite
 */
export default class SubtleCryptoExtension extends SubtleCrypto implements ISubtleCrypto {
  private keyStore: IKeyStore;
  private cryptoFactory: CryptoFactory;

  constructor(cryptoFactory: CryptoFactory) {
    super();
    this.keyStore = cryptoFactory.keyStore;
    this.cryptoFactory = cryptoFactory;
  }

  /**
   * Sign with a key referenced in the key store
   * @param algorithm used for signature
   * @param keyReference points to key in the key store
   * @param data to sign
   * @returns The signature in the requested algorithm
   */
  public async signByKeyStore(algorithm: CryptoAlgorithm, keyReference: string, data: BufferSource): Promise<ArrayBuffer> {
    const jwk: PublicKey = await <Promise<PublicKey>>this.keyStore.get(keyReference, false);
    const crypto: SubtleCrypto = CryptoHelpers.getSubtleCryptoForTheAlgorithm(this.cryptoFactory, algorithm);
    const keyImportAlgorithm = CryptoHelpers.getKeyImportAlgorithm(algorithm, jwk);
    
    const key = await crypto.importKey('jwk', jwk, keyImportAlgorithm, true, ['sign']);
    return await <PromiseLike<ArrayBuffer>>crypto.sign(jwk.kty === KeyType.EC ? <EcdsaParams>algorithm: <RsaPssParams>algorithm, key, <ArrayBuffer>data);
  }
        
  /**
   * Encrypt with a jwk key referenced in the key store
   * @param algorithm used for encryption
   * @param jwk Json web key public key
   * @param data to encrypt
   */
  public async encryptByJwk(algorithm: CryptoAlgorithm, jwk: PublicKey | JsonWebKey, data: BufferSource): Promise<ArrayBuffer> {
    const keyImportAlgorithm = CryptoHelpers.getKeyImportAlgorithm(algorithm, jwk);
    const crypto: SubtleCrypto = CryptoHelpers.getSubtleCryptoForTheAlgorithm(this.cryptoFactory, algorithm);
    const key = await crypto.importKey('jwk', jwk, keyImportAlgorithm, true, ['encrypt']);
    return await <PromiseLike<ArrayBuffer>>crypto.encrypt(algorithm, key, <ArrayBuffer>data);
  }
        
}

 