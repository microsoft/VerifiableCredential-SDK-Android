/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

require('es6-promise').polyfill();
import 'isomorphic-fetch';
import Identifier from '../Identifier';
import IdentifierDocument from '../IdentifierDocument';
import Resolver from './Resolver';
import UserAgentOptions from '../UserAgentOptions';
declare var fetch: any;

/**
 * Fetches DID Documents from remote resolvers over http
 * @class
 * @implements Resolver
 */
export default class HttpResolver implements Resolver {

  /**
   * Constructs an instance of the HttpResolver class.
   * @param url of the remote resolver.
   * @param options for configuring the resolver.
   */
  constructor (public url: string, public options: UserAgentOptions) {
    // Format the url
    const slash = url.endsWith('/') ? '' : '/';
    this.url = `${url}${slash}1.0/identifiers/`;
  }

  /**
   * Sends a fetch request to the resolver URL including the
   * specified identifier.
   * @param identifier to resolve.
   */
  public async resolve (identifier: Identifier): Promise<IdentifierDocument> {
    const query = `${this.url}${identifier}`;
    return new Promise((resolve, reject) => {
      let timer = setTimeout(
        () => reject(new Error('Fetch timed out.')), 1000 * this.options.timeoutInSeconds
      );

      // Now call the actual fetch with the updated options
      fetch(query)
        .then(
          async (response: any) => {
            if (!response.ok) {
              let error: Error;
              switch (response.status) {
                case 404:
                  error = new Error(`Identifier document not found for '${identifier}'`);
                  break;
                default:
                  error = new Error(`Resolver at '${this.url}' returned an error with '${response.statusText}'`);
              }

              // Reject the promise
              reject(error);
            }

            const identifierDocument = new IdentifierDocument(await response.json(), this.options);
            resolve(identifierDocument);
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
