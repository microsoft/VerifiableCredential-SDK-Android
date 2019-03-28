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
