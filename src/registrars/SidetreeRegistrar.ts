/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

require('es6-promise').polyfill();
import 'isomorphic-fetch';
import Registrar from './Registrar';
import Identifier from '../Identifier';
import IdentifierDocument from '../IdentifierDocument';
import UserAgentOptions from '../UserAgentOptions';
import UserAgentError from '../UserAgentError';
import { Secp256k1CryptoSuite, CryptoFactory, JwsToken } from '@decentralized-identity/did-auth-jose';
import { DidKey, KeyExport } from '@decentralized-identity/did-common-typescript';
declare var fetch: any;

/**
 * Registrar implementation for the Sidetree (ION) network
 */
export default class SidetreeRegistrar implements Registrar {
  private timeoutInMilliseconds: number;

  /**
   * Constructs a new instance of the Sidetree registrar
   * @param url to the regsitration endpoint at the registrar
   * @param options to configure the resis
   */
  constructor (public url: string, public options?: UserAgentOptions) {
    // Format the url
    const slash = url.endsWith('/') ? '' : '/';
    this.url = `${url}${slash}`;

    this.timeoutInMilliseconds =
      1000 *
      (!this.options || !this.options.timeoutInSeconds
        ? 30
        : this.options.timeoutInSeconds);
  }

  /**
   * Sign the body for the registar
   * @param body Body to sign
   */
  public async signRequest (
    body: string,
    didKey: DidKey
  ): Promise<string> {
    const cryptoFactory = new CryptoFactory([new Secp256k1CryptoSuite()]);
    const token = new JwsToken(body, cryptoFactory);
    const privateKey = await didKey.getJwkKey(KeyExport.Private);
    const signedRegistrationRequest = await token.sign(privateKey);
    return signedRegistrationRequest;
  }

  /**
   * @inheritdoc
   */
  public async register (
    identifierDocument: IdentifierDocument,
    didKey: DidKey
  ): Promise<Identifier> {
    const bodyString = JSON.stringify(identifierDocument);

    return new Promise(async (resolve, reject) => {
      let timer = setTimeout(
        () => reject(new UserAgentError('Fetch timed out.')),
        this.timeoutInMilliseconds
      );

      const signedRequest = await this.signRequest(bodyString, didKey);

      const fetchOptions = {
        method: 'POST',
        body: signedRequest,
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': bodyString.length.toString()
        }
      };

      // Now call the actual fetch with the updated options
      const response = await fetch(this.url, fetchOptions);

      // Got a response so clear the timer
      clearTimeout(timer);

      if (!response.ok) {
        const error = new UserAgentError(
          'Failed to register the identifier document.'
        );
        reject(error);
        return;
      }

      const responseJson = await response.json();
      const identifier = new Identifier(responseJson, this.options);
      resolve(identifier);
    });
  }
}
