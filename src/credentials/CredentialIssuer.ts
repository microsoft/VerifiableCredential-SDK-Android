/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from './ICredential';
import UserAgentError from '../UserAgentError';
import Identifier from '../Identifier';
import CredentialManifest from './CredentialManifest';
import 'isomorphic-fetch';
declare var fetch: any;

/**
 * Class for obtaining
 * credentials from an issuer.
 */
export default class CredentialIssuer {

  /**
   * The identifier for the issuer.
   */
  public readonly identifier: Identifier;

  /**
   * The manifest of the credential being issued
   */
  public readonly manifest: CredentialManifest;

  /**
   * Constructs an instance of the credential issuer
   * based on the specified credential manifest.
   * @param identifier for the issuer.
   * @param manifest credential manifest for specific credential.
   */
  constructor (identifier: Identifier, manifest: any) {
    this.identifier = identifier;
    this.manifest = new CredentialManifest(manifest);
  }

  /**
   * Constructs an instance of the credential issuer
   * based on the specified credential manifest.
   * TODO: check if manifest param is id in hub of credential manifest.
   * @param identifier for the issuer.
   * @param manifest credential manifest object or endpoint string of manifest.
   */
  public static async create (identifier: Identifier, manifest: CredentialManifest | string) {

    let manifestInstance: any;

    if (typeof(manifest) === 'string') {
      const response = await fetch(manifest);
      if (!response.ok) {
        let error: Error;
        switch (response.status) {
          case 404:
            error = new UserAgentError(`Failed to request a credential manifest from the issuer \'${identifier.id}.\'`);
            break;
          default:
            error = new UserAgentError(`'${manifest}' returned an error with \'${response.statusText}\'`);
        }
        throw error;
      }
      manifestInstance = await response.json();
    } else {
      manifestInstance = manifest;
    }
    return new CredentialIssuer(identifier, manifestInstance);
  }

  /**
   * Gets the array of languages supported
   * by the manifest.
   */
  // public get language (): Array<string> {
  //   console.log(this.identifier);
  //   return this.manifest.language || [];
  // }

  /**
   * Requests a new credential from the issuer,
   * providing a self-issued credential with the inputs
   * specified in the credential manifest.
   * @param inputCredential containing the inputs as specified
   * in the credential manifest.
   */
  public async requestCredential (inputCredential: ICredential): Promise<ICredential> {

    // Sign the credential, will need to
    // TODO: add sign and encrypt methods on identifier class
    const serializedCredential = JSON.stringify(inputCredential);

    return new Promise(async (resolve, reject) => {
      const timer = setTimeout(
        () => reject(new UserAgentError(`Requesting a credential from '${this.manifest.endpoint}' timed out`)),
        30000 // 30s
      );

      const fetchOptions = {
        method: 'POST',
        body: inputCredential,
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': serializedCredential.length.toString()
        }
      };

      // Now call the actual fetch with the updated options
      const response = await fetch(this.manifest.endpoint, fetchOptions);

      // Got a response so clear the timer
      clearTimeout(timer);

      if (!response.ok) {
        const error = new UserAgentError(
          `Failed to request a credential from the issuer '${this.identifier.id}.'`
        );
        reject(error);
        return;
      }

      const responseJson = await response.json();
      const credential: ICredential = responseJson.body;
      resolve(credential);
    });
  }
}
