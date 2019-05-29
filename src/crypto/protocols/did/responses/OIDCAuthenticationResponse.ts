/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * OpenID Connect Self-Issued ID Token Response.
 */
export default interface AuthenticationResponse {
  /** 
   * MUST be set to 'https://self-issued.me'. 
   */
  iss: 'https://self-issued.me';
  /** 
   * DID used to sign the response 
   */
  sub: string;
  /** 
   * The redirect url as specified in the OIDC self-issued protocol 
   */
  aud: string;
  /** 
   * Nonce of the challenge 
   */
  nonce: string;
  /** 
   * Expiration as a UTC datetime 
   */
  exp: number;
  /** 
   * Issued at as a UTC datetime 
   */
  iat: number;
  /** 
   * the public key used to check the signature of the ID Token 
   */
  sub_jwk: object;
  /** 
   * DID used to sign the response 
   */
  did: string;
  /** 
   * Opaque value used by issuer for state, will contain values such as session ID. 
   */
  state: string | undefined;
}
