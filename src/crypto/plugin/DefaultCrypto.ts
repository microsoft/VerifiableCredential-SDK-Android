/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import ISubtleCrypto from './ISubtleCrypto'
import { SubtleCrypto } from 'webcrypto-core';
import IKeyStore from '../keystore/IKeyStore';
import CryptoFactory from './CryptoFactory';
import CryptoHelpers from '../utilities/CryptoHelpers';
import PublicKey from '../keys/PublicKey';

/**
 * Default crypto suite
 */
export default class DefaultCrypto extends SubtleCrypto implements ISubtleCrypto {
  private keyStore: IKeyStore;
  private cryptoFactory: CryptoFactory;

  constructor(keyStore: IKeyStore, cryptoFactory: CryptoFactory) {
    super();
    this.keyStore = keyStore;
    this.cryptoFactory = cryptoFactory;
  }

  /**
   * Sign with a key referenced in the key store
   * @param algorithm used for signature
   * @param keyReference points to key in the key store
   * @param data to sign
   * @returns The signature in the requested algorithm
   */
  public async signByKeyStore(algorithm: RsaPssParams | EcdsaParams | AesCmacParams, keyReference: string, data: BufferSource): Promise<ArrayBuffer> {
    const jwk: PublicKey = await <Promise<PublicKey>>this.keyStore.get(keyReference, false);
    const crypto: ISubtleCrypto = CryptoHelpers.getSubtleCrypto(this.cryptoFactory, algorithm);
    const keyImportAlgorithm = CryptoHelpers.getKeyImportAlgorithm(algorithm, jwk);
    const key = await crypto.importKey('jwk', jwk, keyImportAlgorithm, true, ['sign']);
    return await <PromiseLike<ArrayBuffer>>crypto.sign(algorithm, key, data);
  }        
}

