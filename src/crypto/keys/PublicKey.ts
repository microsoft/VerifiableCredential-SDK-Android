/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import { KeyType } from './KeyTypeFactory'
import { Url } from 'url';
import { KeyUse } from '@decentralized-identity/did-crypto-typescript';
const jose = require('node-jose');

/**
 * JWK key operations
 */
export enum KeyOperation {
  Sign = 'sign',
  Verify = 'verify',
  Encrypt = 'encrypt',
  Decrypt = 'decrypt',
  WrapKey = 'wrapKey',
  UnwrapKey = 'unwrapKey',
  DeriveKey = 'deriveKey',
  DeriveBits = 'deriveBits'
}

/**
 * Represents a Public Key in JWK format.
 * @class
 * @abstract
 * @hideconstructor
 */
export default abstract class PublicKey {
  /**
   * Key type
   */
  public kty: KeyType;
   
  /**
   * Key ID
   */
   public kid?: string = '';
  
  /**
   * Intended use
   */
   public use?: KeyUse; // "sig" "enc"
  
  /**
   * Valid key operations (key_ops)
   */
   public key_ops?: KeyOperation[];
  
  /**
   * Algorithm intended for use with this key
   */
   public alg?: string;

   /**
    * Create instance of @class PublicKey
    */
   constructor(key: PublicKey) {
    this.kty = key.kty;
    this.kid = key.kid;
    this.use = key.use;
    this.key_ops = key.key_ops;
    this.alg = key.alg;
   }

  /**
   * Obtains the thumbprint for the jwk parameter
   * @param jwk JSON object representation of a JWK
   */
  static async getThumbprint (publicKey: PublicKey): Promise<string> {
    const key = await jose.JWK.asKey(publicKey);
    const thumbprint = await key.thumbprint('SHA-512');
    return base64url.encode(thumbprint);
  }
}
