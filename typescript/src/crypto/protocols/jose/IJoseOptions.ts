/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PrivateKey from '../../keys/PrivateKey';
import PublicKey from '../../keys/PublicKey';
import CryptoFactory from '../../plugin/CryptoFactory';
import { JwsHeader } from './jws/IJwsGeneralJson';

/**
 * Define different types for the algorithm parameter
 */
export type CryptoAlgorithm = RsaPssParams | EcdsaParams | Algorithm;

/**
 * Interface defining options for JOSE operations.
 */
export interface IJoseOptions {
  // The crypto algorithm suites used for signing
  cryptoFactory: CryptoFactory,

  // The default protected header
  protected?: JwsHeader,

  // The default header
  header?: JwsHeader,

  // The prefix for a kid when key is generated
  kidPrefix?: string,

  // Make the type indexable
  [key: string]: any;
}

/**
 * Interface defining signature options.
 * Need to redefine the location of this interface - todo
 */
export interface IJwsSigningOptions extends IJoseOptions {
}

/**
 * Interface defining encryption options.
 */
export interface IJweEncryptionOptions extends IJoseOptions {
  /**
   * The content encryption algorithm in JWA format
   */ 
  contentEncryptionAlgorithm: string,

  /**
   * The content key encryption key.
   * Remark: Only used for testing with reference data.
   * Should be undefined in production code.
   */
  contentEncryptionKey?: Buffer,

  /**
   * The initial vector.
   * Remark: Only used for testing with reference data.
   * Should be undefined in production code.
   */
  initialVector?: Buffer
}
