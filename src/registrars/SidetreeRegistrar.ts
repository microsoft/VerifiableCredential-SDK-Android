/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

require('es6-promise').polyfill();
import base64Url from 'base64url';
import 'isomorphic-fetch';
import Identifier from '../Identifier';
import IdentifierDocument from '../IdentifierDocument';
import UserAgentError from '../UserAgentError';
import UserAgentOptions from '../UserAgentOptions';
import Multihash from './Multihash';
import Registrar from './Registrar';
const cloneDeep = require('lodash/fp/cloneDeep');
declare var fetch: any;

/**
 * Registrar implementation for the Sidetree (ION) network
 */
export default class SidetreeRegistrar implements Registrar {
  private timeoutInMilliseconds: number;

  /**
   * Constructs a new instance of the Sidetree registrar
   * @param url to the registration endpoint at the registrar
   * @param options to configure the registrar.
   */
  constructor (public url: string, public options?: UserAgentOptions) {
    // Format the url
    this.url = `${url.replace(/\/?$/, '/')}register`;
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
   * to generate and return the identifier.
   *
   * @param identifierDocument for which to generate the identifier.
   */
  public async generateIdentifier (identifierDocument: IdentifierDocument
  ): Promise<Identifier> {

    if (!Array.isArray(identifierDocument.publicKeys) || identifierDocument.publicKeys.length === 0) {
      throw new UserAgentError('At least one public key must be specified in the identifier document.');
    }

    // The genesis document is used for generating the hash,
    // but we need to ensure that the id property of the document
    // if specified is removed beforehand.
    const genesisDocument = cloneDeep(identifierDocument);
    genesisDocument.id = undefined;

    // Hash the document JSON
    const documentBuffer = Buffer.from(JSON.stringify(genesisDocument));
    const hashedDocument = Multihash.hash(documentBuffer, 18);
    const encodedDocument = base64Url.encode(hashedDocument);

    // Now update the identifier property in
    // the genesis document
    genesisDocument.id = `did:ion:${encodedDocument}`;
    return new Identifier(genesisDocument);
  }
}
