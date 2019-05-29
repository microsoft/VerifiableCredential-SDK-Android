/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PublicKey from '../PublicKey';
import { KeyType } from '../KeyTypeFactory';

/**
 * Represents an RSA public key
 * @class
 * @extends PublicKey
 */
export default class RsaPublicKey extends PublicKey {
  /** 
   * Public exponent 
   */
  public e: string | undefined;
  /** 
   * Modulus 
   */
  public n: string | undefined;
  /**
   * Set the EC key type
   */
  kty = KeyType.RSA;
  /**
   * Set the default algorithm
   */
  alg = 'RS256';

  /**
   * Create instance of @class RsaPublicKey
   */
  constructor (key: any) {
    super(key)
    this.alg = key.alg;
    this.key_ops = key.key_ops;
    this.kid = key.kid;
    this.use = key.use;
    this.e = key.e;
    this.n = key.n;
  }
}
