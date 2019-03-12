/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { CredentialInput, DataInput } from './types';

/**
 * context for credentialManifest
 */
const context = 'https://identity.foundation/schemas/credentials';

/**
 * type for credentialManifest
 */
const type = 'CredentialManifest';

/**
 *  Class defining methods and properties for a ClaimManifest object.
 *  based off of the CredentialManifest spec: https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md
 */
export default class CredentialManifest {

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
   * issuer options for things such as the style of the manifest in the UI.
   */
  public issuerOptions: any;

  /**
   * Constructs an instance of the CredentialManifest class from a well-formed credential manifest JSON object.
   * TODO: check that the JSON parameter is valid (yup?)
   */
  constructor (credentialManifest: any) {
    this.credential = credentialManifest.credential;
    this.preconditions = credentialManifest.preconditions;
    this.inputs = credentialManifest.inputs;
    this.issuerOptions = credentialManifest.issuerOptions;
  }

  /**
   * Creates a new instance of the CredentialManifest class.
   */
  public static create (credential: string, preconditions: any, inputs: any, issuerOptions: any) {
    const manifest = {
      '@context': context,
      '@type': type,
      'credential': credential,
      'preconditions': preconditions,
      'inputs': inputs,
      'issuer_options': issuerOptions
    };
    return new CredentialManifest(manifest);
  }

  /**
   * forms the CredentialManifest JSON.
   */
  public form () {
    const manifest = {
      '@context': context,
      '@type': type,
      'credential': this.credential,
      'preconditions': this.preconditions,
      'inputs': this.inputs,
      'issuer_options': this.issuerOptions
    };
    return manifest;
  }

  /**
   * Get the input properties of the manifest
   */
  public getInputProperties () {
    return this.inputs;
  }

  /**
   * Get the display properties of the manifest.
   */
  public getDisplayProperties () {
    return this.issuerOptions.style;
  }

  /**
   * Get the group expressions of the manifest
   */
  public getGroupExpressions () {
    return this.preconditions.groups;
  }

}
