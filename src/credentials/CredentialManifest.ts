/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { CredentialInput, DataInput, OpenIDInput } from 'src/types';
import CredentialManifestIssuerOptions from 'src/credentials/CredentialManifestIssuerOptions';

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
 *  based off of the CredentialManifest spec: {@link https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md}
 */
export default class CredentialManifest {

  /**
   * Name of the credential
   */
  public readonly credential?: string;

  /**
   * Languages supported for localization
   */
  public readonly language?: string[];

  /**
   * the keeper DID whose hub stores the CredentialManifest.
   */
  public readonly keeper?: string;

  /**
   * Version of the CredentialManifest
   */
  public readonly version?: string;

  /**
   * preconditions for the manifest.
   */
  public readonly preconditions?: any;

  /**
   * inputs parameter for manifest.
   */
  public readonly inputs?: (CredentialInput | DataInput | OpenIDInput)[];

  /**
   * issuer options for things such as the style of the manifest in the UI.
   */
  public issuerOptions?: CredentialManifestIssuerOptions;

  /**
   * issuer endpoint
   */
  public endpoint: string;

  /**
   * Constructs an instance of the CredentialManifest class from a well-formed credential manifest JSON object.
   */
  constructor (credentialManifest: any) {
    this.endpoint = credentialManifest.endpoint;
    this.credential = credentialManifest.credential;
    this.language = credentialManifest.language;
    this.keeper = credentialManifest.keeper;
    this.version = credentialManifest.version;
    this.preconditions = credentialManifest.preconditions;
    this.inputs = credentialManifest.inputs;
    this.issuerOptions = credentialManifest.issuer_options;
  }

  /**
   * Creates a new instance of the CredentialManifest class.
   */
  public static create (credential: string,
                        endpoint: string,
                        language: string[],
                        keeper: string,
                        version: string,
                        preconditions: any,
                        inputs: (CredentialInput | DataInput | OpenIDInput)[],
                        issuerOptions: CredentialManifestIssuerOptions) {
    const manifest = {
      '@context': context,
      '@type': type,
      'endpoint': endpoint,
      'language': language,
      'credential': credential,
      'keeper': keeper,
      'version': version,
      'preconditions': preconditions,
      'inputs': inputs,
      'issuer_options': issuerOptions
    };
    return new CredentialManifest(manifest);
  }

  /**
   * serializes the CredentialManifest to JSON.
   */
  public toJSON () {
    return {
      '@context': context,
      '@type': type,
      'endpoint': this.endpoint,
      'credential': this.credential,
      'preconditions': this.preconditions,
      'inputs': this.inputs,
      'issuer_options': this.issuerOptions
    };
  }

  /**
   * Get the keeper did of the CredentialManifest
   */
  public getKeeperDid () {
    return this.keeper;
  }

  /**
   * Get the input properties of the manifest
   */
  public getInputProperties () {
    return this.inputs;
  }
}
