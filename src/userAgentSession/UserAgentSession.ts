/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import JwsToken from '../crypto/protocols/jws/JwsToken';
import UserAgentOptions from '../UserAgentOptions';
import IResolver from '../resolvers/IResolver';
import CryptoFactory from '../crypto/plugin/CryptoFactory';
import KeyStoreInMemory from '../crypto/keyStore/KeyStoreInMemory';
import OIDCAuthenticationRequest from '../crypto/protocols/did/requests/OIDCAuthenticationRequest';
import OIDCAuthenticationResponse from '../crypto/protocols/did/responses/OIDCAuthenticationResponse';
import UserAgentError from '../UserAgentError';
import PublicKey from '../crypto/keys/PublicKey';

/**
 * Class for creating a User Agent Session for sending and verifying
 * Authentication Requests and Responses.
 */
export default class UserAgentSession {

  private sender: Identifier;
  private resolver: IResolver;
  private keyReference: string
  private cryptoFactory: CryptoFactory;
  
  constructor (sender: Identifier, keyReference: string, resolver: IResolver) {
    this.sender = sender;
    this.resolver = resolver;
    this.keyReference = keyReference;
    this.cryptoFactory = (<UserAgentOptions>sender.options).cryptoFactory;
  }

  /**
   * Sign a User Agent Request.
   * @param redirectUrl url that recipient should send response back to.
   * @param nonce nonce that will come back in response.
   * @param claimRequests any claims that sender is requesting from the recipient.
   * @param state optional stringified JSON state opaque object that will come back in response.
   */
  public async signRequest(redirectUrl: string, nonce: string, claimRequests?: any, state?: string, manifestEndpoint?: string) {

    const request: Partial<OIDCAuthenticationRequest> = {
      iss: this.sender.id,
      response_type: 'id_token',
      response_mode: 'form_post',
      client_id: redirectUrl,
      scope: 'openid',
    };

    if (state) {
      Object.assign(request, {state});
    }

    if (nonce) {
      Object.assign(request, {nonce});
    }

    if (claimRequests) {
      Object.assign(request, {claims: {id_token: claimRequests}});
    }

    if (manifestEndpoint) {
      Object.assign(request, {registration: manifestEndpoint});
    }

    return this.sender.sign(request, this.keyReference);
  }

  /**
   * Sign a User Agent Response.
   * @param redirectUrl url that request was sent to.
   * @param nonce nonce to return to sender of the request.
   * @param state opaque object to return to sender of the request.
   * @param claims any claims that request asked for.
   */
  public async signResponse(redirectUrl: string, nonce: string, state?: string, claims?: any) {

    // check if sender has keystore
    if (!this.sender.options || !this.sender.options.keyStore) {
      throw new UserAgentError(`No KeyStore specified for '${this.sender.id}`);
    }

    // get public key of key referenced by keyReference in keystore
    const publicKey = <PublicKey> await this.sender.options.keyStore.get(this.keyReference, true);

    // milliseconds to seconds
    const milliseconds = 1000;
    const expirationTimeOffsetInMinutes = 5;
    const expiration = new Date(Date.now() + milliseconds * 60 * expirationTimeOffsetInMinutes); // 5 minutes from now
    const exp = Math.floor(expiration.getTime() / milliseconds);
    const iat = Math.floor(Date.now() / milliseconds); // ms to seconds

    // create OIDC response object
    let response: OIDCAuthenticationResponse = {
      iss: 'https://self-issued.me',
      sub: await PublicKey.getThumbprint(publicKey),
      aud: redirectUrl,
      nonce,
      state,
      did: this.sender.id,
      sub_jwk: publicKey,
      iat,
      exp
    };

    // add requested claims to response
    response = Object.assign(response, claims);

    // return signed response
    return this.sender.sign(response, this.keyReference);
  }

  /**
   * Verify a request was signed and sent by Identifier.
   * @param jws Signed Payload
   */
  public async verify(jws: string): Promise<any> {

    // get identifier id from key id in header.
    const token : JwsToken = await JwsToken.deserialize(jws, {cryptoFactory: this.cryptoFactory});
    const payload = JSON.parse(token.payload.toString());

    // create User Agent Options for Identifier
    const options = new UserAgentOptions();
    options.resolver = this.resolver;
    options.cryptoFactory = this.cryptoFactory;

    /**
     * If iss parameter is 'https://selfissued.me', payload is an OIDC auth request
     * and did is in the did parameter. Else the payload is a OIDC auth response,
     * so the did is in the iss parameter. If either parameter is undefined,
     * the payload is not formatted properly, so throw an error.
     */
    const issuerIdentifier = payload.iss === 'https://selfissued.me' ? payload.did : payload.iss;

    if (!issuerIdentifier) {
      throw new UserAgentError('Unable to identify issuer of the token.');
    }

    // verify jws and return payload. 
    const identifier = new Identifier(issuerIdentifier, options);
    const verifiedToken = await identifier.verify(jws);
    return JSON.parse(verifiedToken);
  }
}
