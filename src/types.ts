/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Interface for defining a identifier document
 * public key.
 */
export interface PublicKey {
  /**
   * The id of the public key in the format
   * #{keyIdentifier}.
   */
  id: string;

  /**
   * The type of the public key.
   */
  type: string;

  /**
   * The owner of the key.
   */
  owner?: string;

  /**
   * The JWK public key.
   */
  publicKeyJwk: any;
}

/**
 * Interface for defining a identifier document
 * authentication reference.
 */
export interface AuthenticationReference {
  /**
   * The type of the authentication reference.
   */
  type: string;

  /**
   * A public key reference in the format
   * #{keyIdentifier}.
   */
  publicKeyReference: string;
}

/**
 * Interface for defining a identifier document
 * service reference.
 */
export interface ServiceReference {
  /**
   * The type of the service reference.
   */
  type: string;

  /**
   * A public key reference in the format
   * #{keyIdentifier}.
   */
  publicKeyReference: string;

  /**
   * The service endpoint for the
   * service reference
   */
  serviceEndpoint: ServiceEndpoint;
}

/**
 * Interface for defining a identifier document
 * service reference endpoint.
 */
export interface ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  context: string;

  /**
   * The type of the service reference.
   */
  type: string;
}

/**
 * Interface for defining a identifier document
 * service reference endpoint for a host.
 */
export interface HostServiceEndpoint extends ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  locations: Array<string>;
}

/**
 * Interface for defining a identifier document
 * service reference endpoint for a user.
 */
export interface UserServiceEndpoint extends ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  instances: Array<string>;
}

/**
 * Interface for defining a URI as outlined in the claim spec.
 */
export interface UriDescription {
  /**
   * the type of URI.
   */
  '@type': string;

  /**
   * the actual URI.
   */
  uri: string;

  /**
   * the description of the URI.
   */
  description: string;
}

/**
 * Interface for data inputs that are allowed on a CredentialManifest.
 */
export interface DataInput {
  /**
   * type of input
   */
  type: 'data';

  /**
   * a variety of proofs/prerequisites that can be accepted.
   * e.g. [A]
   */
  group: Array<string>;

  /**
   * what data input is used for.
   * e.g. routing_number
   */
  field: string;

  /**
   * contraints on the input
   * e.g. maxLength: 9
   */
  value: any;
}

/**
 * Interface for credential inputs that are allowed on a CredentialManifest.
 */
export interface CredentialInput {
  /**
   * type of input
   */
  type: 'credential';

  /**
   * a variety of proofs/prerequisites that can be accepted.
   * e.g. [A]
   */
  group: Array<string>;

  /**
   * Schema used for the specific credential.
   */
  schema: string;

  /**
   * constraints on the credential
   * e.g. what issuers will be accepted.
   */
  constraints: {
    /**
     * issuers that are allowed to have issued claim requested.
     */
    issuers: Array<string>
  };
}

/**
 * Interface for openid request input that are allowed on a CredentialManifest.
 */
export interface OpenIDInput {
  /**
   * Type of Input
   */
  type: 'openid';

  /**
   * a variety of proofs/prerequisites that can be accepted.
   * e.g. [A]
   */
  group: Array<string>;

  /**
   * redirect URL.
   */
  redirect: string;

  /**
   * parameters for the openid request.
   */
  parameters: any;
}
