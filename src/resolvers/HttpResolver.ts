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
import UserAgentError from '../UserAgentError';
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
    const query = `${this.url}${identifier.id}`;
    return new Promise(async (resolve, reject) => {
      let timer = setTimeout(
        () => reject(new UserAgentError('Fetch timed out.')), 1000 * this.options.timeoutInSeconds
      );

      // Now call the actual fetch with the updated options
      const response = await fetch(query);

      // Got a response so clear the timer
      clearTimeout(timer);

      // Check if the response was OK, and
      // if not return the appropriate error
      if (!response.ok) {
        let error: Error;
        switch (response.status) {
          case 404:
            error = new UserAgentError(`Identifier document not found for '${identifier.id}'`);
            break;
          default:
            error = new UserAgentError(`Resolver at '${this.url}' returned an error with '${response.statusText}'`);
        }

        // Reject the promise
        reject(error);
        return;
      }

      const responseJson = await response.json();
      const identifierDocument = new IdentifierDocument(responseJson.document || responseJson);
      resolve(identifierDocument);
      return;
    });
  }
}
