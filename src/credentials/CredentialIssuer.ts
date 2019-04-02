/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from './ICredential';
import UserAgentError from '../UserAgentError';

/**
 * Class for obtaining
 * credentials from an issuer.
 */
export default class CredentialIssuer {

  /**
   * Constructs an instance of the credential issuer
   * based on the specified credential manifest.
   * @param manifest for the issuer.
   */
  constructor (private manifest: CredentialManifest) {
  }

  /**
   * Gets the array of languages supported
   * by the manifest.
   */
  public get language (): Array<string> {
    return this.manifest.language || [];
  }

  /**
   * Requests a new credential from the issuer,
   * providing a self-issued credential with the inputs
   * specified in the credential manifest.
   * @param inputCredential containing the inputs as specified
   * in the credential manifest.
   */
  public async requestCredential (inputCredential: ICredential): Promise<ICredential> {

    // Sign the credential, will need to
    // add sign and encrypt methods on identifier class
    const serializedCredential = JSON.stringify(inputCredential);
    const signedCredential = inputCredential.issuedBy.sign(serializedCredential);
    const encryptedCredential = inputCredential.issuedTo.encrypt(signedCredential);

    return new Promise(async (resolve, reject) => {
      const timer = setTimeout(
        () => reject(new UserAgentError(`Requesting a credential from '${this.manifest.endpoint}' timed out`)),
        30000 // 30s
      );

      const fetchOptions = {
        method: 'POST',
        body: encryptedCredential,
        headers: {
          'Content-Type': 'application/jose',
          'Content-Length': encryptedCredential.length.toString()
        }
      };

      // Now call the actual fetch with the updated options
      const response = await fetch(this.manifest.endpoint, fetchOptions);

      // Got a response so clear the timer
      clearTimeout(timer);

      if (!response.ok) {
        const error = new UserAgentError(
          'Failed to request a credential from the issuer.'
        );
        reject(error);
        return;
      }

      const responseJson = await response.json();
      const credential: ICredential = JSON.parse(responseJson);
      resolve(credential);
    });
  }
}
