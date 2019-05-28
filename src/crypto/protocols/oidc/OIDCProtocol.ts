/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IKeyStore from '../../keyStore/IKeyStore';
import IResolver from '../../../resolvers/IResolver';
import CryptoOperations from '../../plugin/CryptoOperations';
import CryptoFactory from '../../plugin/CryptoFactory';
import OIDCAuthenticationRequest from './requests/OIDCAuthenticationRequest';
import OIDCAuthenticationResponse from './responses/OIDCAuthenticationResponse';
import JwsToken from '../jws/JwsToken';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import PublicKey from '../../keys/PublicKey';
import UserAgentError from '../../../UserAgentError';
import { TSMap } from 'typescript-map';
import { stringify } from 'querystring';

/**
 * Named arguments to construct an Authentication object
 */
export interface OIDCProtocolOptions {
  /** 
   * A dictionary with the did document key id mapping to private key references in the keystore 
   */
  keyReferences: string[];
  /** 
   * The keystore
   */
  keyStore: IKeyStore;
  /** 
   * DID Resolver used to retrieve public keys 
   */
  resolver: IResolver;
  /** 
   * Optional parameter to customize supported CryptoSuites 
   */
  suites?: CryptoOperations;
  /** 
   * Optional parameter to change the amount of time a token is valid in minutes
   */
  tokenValidDurationInMinutes: number;
}

/**
 * Class for decrypting and verifying, or signing and encrypting content in an End to End DID Authentication format
 */
export default class OIDCProtocol {

  // need to support encryption and signature keys

  /** 
   * DID Resolver used to retrieve public keys 
   */
  private resolver: IResolver;
  /** 
   * The amount of time a token is valid in minutes
   */
  private tokenValidDurationInMinutes: number = 5;
  /** 
   * Reference to Private keys of the authentication owner 
   */
  private keyReferences: string[];
  /** 
   * The keystore 
   */
  private keyStore: IKeyStore;
  /** 
   * Factory for creating JWTs and public keys 
   */
  private factory: CryptoFactory;

  /**
   * Authentication constructor
   * @param options Arguments to a constructor in a named object
   */
  constructor (options: OIDCProtocolOptions) {
    this.resolver = options.resolver;
    this.tokenValidDurationInMinutes = options.tokenValidDurationInMinutes;
    this.keyStore = options.keyStore;
    this.keyReferences = options.keyReferences;

    this.factory = new CryptoFactory(this.keyStore, options.suites);
  }

  /**
   * Signs the AuthenticationRequest with the private key of the Requester and returns the signed JWT.
   * @param request well-formed AuthenticationRequest object
   * @returns the signed compact JWT.
   */
  public async signAuthenticationRequest (request: OIDCAuthenticationRequest): Promise<string> {
    if (request.response_type !== 'id_token' || request.scope !== 'openid') {
      throw new Error('Authentication Request not formed correctly');
    }

    const jwsToken = new JwsToken({ cryptoFactory: this.factory });

    // convert request to a buffer
    const requestBuffer = Buffer.from(JSON.stringify(request));

    // for signing always use last key
    const referenceToStoredKey = this.keyReferences[this.keyReferences.length - 1];

    //sign, serialize, and return request.
    const signed = await jwsToken.sign(referenceToStoredKey, requestBuffer, ProtectionFormat.JwsCompactJson);
    return signed.serialize(ProtectionFormat.JwsCompactJson);
  }

  /**
   * Verifies signature on request and returns AuthenticationRequest.
   * @param request Authentiation Request as a buffer or string.
   */
  public async verifyAuthenticationRequest (request: Buffer | string, publicKey: PublicKey): Promise<OIDCAuthenticationRequest> {
    let jwsToken: JwsToken;
    if (request instanceof Buffer) {
      jwsToken = JwsToken.create(request.toString(), { cryptoFactory: this.factory });
    } else {
      jwsToken = JwsToken.create(request, { cryptoFactory: this.factory });
    }

    // check to see if token signed by publicKey.
    const verified = await jwsToken.verify(publicKey);

    if (verified) {
      const payload = jwsToken.getPayload().toString();
      return <OIDCAuthenticationRequest> JSON.parse(payload);
    } else {
      throw new UserAgentError(`Unable to verify OIDC Request`);
    }
  }

  /**
   * Given a challenge, forms a signed response using a given DID that expires at expiration, or a default expiration.
   * @param authRequest Challenge to respond to
   * @param responseDid The DID to respond with
   * @param claims Claims that the requester asked for
   * @param expiration optional expiration datetime of the response
   * @param keyReference pointing to the signing key
   */
  public async createAndSignAuthenticationResponse (authRequest: OIDCAuthenticationRequest, responseDid: string, claims: any, expiration?: Date)
  : Promise<string> {
    // for signing always use last key
    const referenceToStoredKey = this.keyReferences[this.keyReferences.length - 1];

    const publicKey: PublicKey = <PublicKey> await this.keyStore.get(referenceToStoredKey, true);
    const base64UrlThumbprint = await PublicKey.getThumbprint(publicKey);

    // milliseconds to seconds
    const milliseconds = 1000;
    if (!expiration) {
      const expirationTimeOffsetInMinutes = 5;
      expiration = new Date(Date.now() + milliseconds * 60 * expirationTimeOffsetInMinutes); // 5 minutes from now
    }
    const iat = Math.floor(Date.now() / milliseconds); // ms to seconds
    let response: OIDCAuthenticationResponse = {
      iss: 'https://self-issued.me',
      sub: base64UrlThumbprint,
      aud: authRequest.client_id,
      nonce: authRequest.nonce,
      exp: Math.floor(expiration.getTime() / milliseconds),
      iat,
      sub_jwk: publicKey,
      did: responseDid,
      state: authRequest.state
    };

    response = Object.assign(response, claims);
    const responseBuffer = Buffer.from(JSON.stringify(response));

    // add iat and exp headings to jwsToken.
    const header: TSMap<string, string> = new TSMap<string, string>();
    header.set('iat', iat.toString());
    header.set('exp',  Math.floor(expiration.getTime() / milliseconds).toString());
    const signingOptions = {
      cryptoFactory: this.factory,
      header
    };

    const jwsToken = new JwsToken(signingOptions);
    const signed = await jwsToken.sign(referenceToStoredKey, responseBuffer, ProtectionFormat.JwsCompactJson);
    return signed.serialize(ProtectionFormat.JwsCompactJson);
  }

  /**
   * Verifies the signature on a AuthenticationResponse and returns a AuthenticationResponse object
   * @param authResponse AuthenticationResponse to verify as a string or buffer
   * @returns the authenticationResponse as a AuthenticationResponse Object
   */
  public async verifyAuthenticationResponse (authResponse: Buffer | string, publicKey: PublicKey): Promise<OIDCAuthenticationResponse> {
    const clockSkew = 5 * 60 * 1000; // 5 minutes
    let jwsToken: JwsToken;
    if (authResponse instanceof Buffer) {
      jwsToken = JwsToken.create(authResponse.toString(), { cryptoFactory: this.factory });
    } else {
      jwsToken = JwsToken.create(authResponse, { cryptoFactory: this.factory });
    }
    const response = JSON.parse(jwsToken.getPayload().toString());
    const exp = response.exp;
    if (exp) {
      if (exp * 1000 + clockSkew < Date.now()) {
        throw new Error('Response expired');
      }
    }

    const verified = await jwsToken.verify(publicKey);

    if (verified) {
      if (response.did !== publicKey.kid) {
        throw new UserAgentError(`Signing DID: '${publicKey.kid}' does not match issuer`);
      }
      return response;
    } else {
      throw new UserAgentError(`OIDC Response could not be verified by key owned by '${publicKey.kid}'`);
    }
  }
}
