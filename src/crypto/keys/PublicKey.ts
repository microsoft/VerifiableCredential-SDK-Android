/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import { KeyType } from './KeyType'
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
  // Key type
  public kty: KeyType | undefined;
  // Key ID 
  public kid: string = '';
  // Intended use
  public use?: KeyUse; // "sig" "enc"
  // Valid key operations (key_ops)
  public key_ops?: KeyOperation[];
  // Algorithm intended for use with this key
  public alg?: string;
  // A resource for a X.509 public key certificate
  public x5u?: Url;
  // One or more PKIX certificates as base64 DER
  public x5c?: string[];
  // Base64URL SHA-1 thumbprint of the DER of an X.509 certificate
  public x5t?: string;
  // base64URL SHA-256 thumbprint of the DER of the X.509 certificate
  public x5tS256?: string;

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
