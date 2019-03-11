/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { CredentialInput, DataInput } from './types';
/**
 * Interface defining methods and properties for a ClaimManifest object.
 */
export default class CredentialManifest {

  /**
   * context for credentialManifest
   */
  public readonly context = 'https://identity.foundation/schemas/credentials';

  /**
   * type for credentialManifest
   */
  public readonly type = 'CredentialManifest';

  /**
   * Name of the credential
   */
  public credential: string;

  /**
   * preconditions for the manifest.
   */
  public preconditions: any;

  /**
   * inputs parameter for manifest.
   */
  public inputs: Array<CredentialInput | DataInput>;

  /**
   * issuer options for the style of the manifest in the UI.
   */
  public issuerOptions: any;

  /**
   * Contructs an instance of the CredentialManifest class
   */
  constructor (credential: string, preconditions: any, inputs: any, issuerOptions: any) {
    this.credential = credential;
    this.preconditions = preconditions;
    this.inputs = inputs;
    this.issuerOptions = issuerOptions;
  }

  /**
   * Creates a new instance of the CredentialManifest class.
   */
  public static create (parameters: any) {
    return new CredentialManifest(parameters.credential, parameters.preconditions, parameters.inputs, parameters.issuerOptions);
  }

  /**
   * forms the CredentialManifest JSON.
   */
  public form () {
    const manifest = {
      '@context': this.context,
      '@type': this.type,
      'credential': this.credential,
      'preconditions': this.preconditions,
      'inputs': this.inputs,
      'issuer_options': this.issuerOptions
    };
    return manifest;
  }

}
