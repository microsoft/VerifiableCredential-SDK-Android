/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import RsaPublicKey from './RsaPublicKey';
import PrivateKey from '../PrivateKey';
import { KeyUse } from '../KeyUseFactory'
import PublicKey from '../PublicKey';

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
  public q: string | undefined;
  /** 
   * Private dp
   */
  public dp: string | undefined;
  /** 
   * Private dq
   */
  public dq: string | undefined;
  /** 
   * Private qi 
   */
  public qi: string | undefined;

  /**
   * Gets the corresponding public key
   * @returns The corresponding {@link PublicKey}
   */
   public getPublicKey (): PublicKey {
    return <RsaPublicKey> {
      kty: this.kty,
      kid: this.kid,
      n: this.n,
      e: this.e,
      use: KeyUse.Signature,
      alg: this.alg
    };
  }
}
