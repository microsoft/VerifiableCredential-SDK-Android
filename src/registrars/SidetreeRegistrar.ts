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
    this.url = `${url}${slash}register`;

    this.timeoutInMilliseconds =
      1000 *
      (!this.options || !this.options.timeoutInSeconds
        ? 30
        : this.options.timeoutInSeconds);
  }

  /**
   * @inheritdoc
   */
  public async register (
    identifierDocument: IdentifierDocument
  ): Promise<Identifier> {
    const bodyString = JSON.stringify(identifierDocument);

    return new Promise(async (resolve, reject) => {
      let timer = setTimeout(
        () => reject(new UserAgentError('Fetch timed out.')),
        this.timeoutInMilliseconds
      );

      const fetchOptions = {
        method: 'POST',
        body: bodyString,
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

   /**
   * Uses the specified input to create a basic Sidetree
   * compliant identifier document and then hashes the document
   * in accordance with the Sidetree protocol specification
   * to generate and return the identifier for the input.
   *
   * @param input.publicKey to include in the basic Sidetree identifier 
   * document.
   */
  public async generateIdentifier(input: any
  ): Promise<Identifier> {

    if (!input && !input.publicKey) {
      throw new UserAgentError('The input provided to the SidetreeRegistrar.generateIdentifier() does not include the required public key.');
    }

  /*   {
      "@context": "https://w3id.org/did/v1",
      "publicKey": [{
        "id": "#key1",
        "type": "Secp256k1VerificationKey2018",
        "publicKeyHex": "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
      }],
      "service": [{
        "id": "IdentityHub",
        "type": "IdentityHub",
        "serviceEndpoint": {
          "@context": "schema.identity.foundation/hub",
          "@type": "UserServiceEndpoint",
          "instance": ["did:bar:456", "did:zaz:789"]
        }
      }]
    } */

    const identifierDocument = Object.assign({
      '@context': 'https://w3id.org/did/v1',
    }, input.publicKey);
  }
}
