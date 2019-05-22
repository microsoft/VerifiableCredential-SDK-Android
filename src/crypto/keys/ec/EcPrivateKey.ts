/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 
import EcPublicKey from './EcPublicKey';
import PrivateKey from '../PrivateKey';
import PublicKey from '../PublicKey';
import { KeyUse } from '../KeyUse'

/**
 * Represents an Elliptic Curve private key
 * @class
 * @extends PrivateKey
 */
export default class EcPrivateKey extends EcPublicKey implements PrivateKey {
  /** 
   * ECDSA w/ secp256k1 Curve 
   */
  readonly alg: string = 'ES256K';

  /** 
   * Private exponent 
   */
  public d: string | undefined;

  getPublicKey (): PublicKey {
    return <EcPublicKey>{
      kty: this.kty,
      kid: this.kid,
      crv: this.crv,
      x: this.x,
      y: this.y,
      use: KeyUse.Signature,
      alg: this.alg
    };
  }
}
