/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * OpenID Connect Self-Issued Authentication Request.
 */
export default interface OIDCAuthenticationRequest {
  /** 
   * DID of the issuer of the request. This should match the signature
   */
  iss: string;
  /** 
   * MUST be set as 'id_token' in order to match OIDC self-issued protocol 
   */
  response_type: 'id_token';
  /** 
   * The redirect url as specified in the OIDC self-issued protocol
   */
  client_id: string;
  /** 
   * MUST be set to 'openid' 
   */
  scope: 'openid';
  /** 
   * Opaque value used by issuer for state 
   */
  state: string | undefined;
  /** 
   * Request Nonce 
   */
  nonce: string;
  /** 
   * Claims that are requested 
   */
  claims: {id_token: {[key: string]: any}};
}
