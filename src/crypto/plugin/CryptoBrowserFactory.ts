/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoOperations, { CryptoSuiteMap } from './CryptoOperations';
import SubtleCryptoBrowserOperations from './SubtleCryptoBrowserOperations';
import IKeyStore from '../keyStore/IKeyStore';
import CryptoFactory from './CryptoFactory';

/**
 * Utility class to handle all CryptoSuite dependency injection
 */
export default class CryptoBrowserFactory extends CryptoFactory{

  /**
   * Constructs a new CryptoRegistry
   * @param keyStore used to store private jeys
   * @param crypto The suite to use for dependency injection
   */
  constructor (keyStore: IKeyStore, crypto: CryptoOperations) {
    super(keyStore, crypto);
    this.messageSigners['ES256K'] = new SubtleCryptoBrowserOperations();
  }
}
