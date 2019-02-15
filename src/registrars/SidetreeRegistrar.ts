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

  /**
   * Constructs a new instance of the Sidetree registrar
   * @param url to the regsitration endpoint at the registrar
   * @param options to configure the resis
   */
  constructor (public url: string, public options: UserAgentOptions) {
    // Format the url
    const slash = url.endsWith('/') ? '' : '/';
    this.url = `${url}${slash}register`;
  }

  /**
   * @inheritdoc
   */
  public async register (identifierDocument: IdentifierDocument): Promise<Identifier> {
    const bodyString = JSON.stringify(identifierDocument);

    return new Promise((resolve, reject) => {
      let timer = setTimeout(
        () => reject(new Error('Fetch timed out.')), 1000 * this.options.timeoutInSeconds
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
      fetch(this.url, fetchOptions)
        .then(
          async (response: any) => {
            if (!response.ok) {
              reject(new UserAgentError('Failed to register the identifier document.'));
            }

            const identifier = new Identifier(await response.json(), this.options);
            resolve(identifier);
          },
          (error: any) => reject(error)
        )
        .finally(() => {
          // Clear the timer
          clearTimeout(timer);
        });
    });
  }
}
