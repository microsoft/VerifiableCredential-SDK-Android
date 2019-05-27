/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import RsaPublicKey from './RsaPublicKey';
import PrivateKey from '../PrivateKey';
import PublicKey from '../PublicKey';
const clone = require('clone');

/**
 * Represents an Elliptic Curve private key
 * @class
 * @extends PrivateKey
 */
export default class RsaPrivateKey extends RsaPublicKey implements PrivateKey {

  /** 
   * Private exponent 
   */
  public d: string | undefined;
  /** 
   * Prime p
   */
  public p: string | undefined;
  /** 
   * Prime q
   */
  public q: string;
  /** 
   * Private dp
   */
  public dp: string;
  /** 
   * Private dq
   */
  public dq: string;
  /** 
   * Private qi 
   */
  public qi: string;

  /**
   * Create instance of @class RsaPrivateKey
   */
  constructor (key: any) {
    super(key)
    this.d = key.d;
    this.p = key.p;
    this.q = key.q;
    this.dp = key.dp;
    this.dq = key.dq;
    this.qi = key.qi;
  }

  /**
   * Gets the corresponding public key
   * @returns The corresponding {@link PublicKey}
   */
   public getPublicKey (): PublicKey {
    const publicKey = clone(this);
    delete publicKey.d, publicKey.p, publicKey.q, publicKey.dp, publicKey.dq, publicKey.qi;
    return publicKey;
  }
}
