/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 
import EcPublicKey from './EcPublicKey';
import PrivateKey from '../PrivateKey';
import PublicKey from '../PublicKey';
const clone = require('clone');

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

  /**
   * Create instance of @class EcPrivateKey
   */
  constructor (key: any) {
    super(key)
    this.d = key.d;
  }

    /**
   * Gets the corresponding public key
   * @returns The corresponding {@link PublicKey}
   */
  public getPublicKey (): PublicKey {
    const publicKey = clone(this);
    delete publicKey.d;
    return publicKey;
  }
}
