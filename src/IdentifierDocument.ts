/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, PublicKey } from './types';
import UserAgentOptions from './UserAgentOptions';
import Identifier from './Identifier';

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 */
export default class IdentifierDocument {
  /**
   * The identifier the document represents.
   */
  public id: string;

  /**
   * The date the document was created.
   */
  public created: Date;

  /**
   * Array of service entries added to the document.
   */
  public publicKeys: Array<PublicKey> = [];

  /**
   * Array of authentication entries added to the document.
   */
  public authenticationReferences: Array<AuthenticationReference> = [];

  /**
   * Array of service entries added to the document.
   */
  public serviceReferences: Array<ServiceReference> = [];

  /**
   * Constructs an instance of the identifier
   * document.
   * @param document from which to create the identifier document.
   * @param options for configuring how to register and resolve identifiers.
   */
  constructor (document: any) {
    // Populate the base properties
    this.id = document.id;
    this.created = document.created;
    this.publicKeys = document.publicKey;
    this.authenticationReferences = document.authentication;
    this.serviceReferences = document.service;
  }

  /**
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * @param publicKeys to include in the document.
   */
  public static create (id: string, publicKeys: Array<PublicKey>, options: UserAgentOptions): IdentifierDocument {
    // Create the document properties
    const identifier = new Identifier(id, options);
    return new IdentifierDocument({ id: identifier, created: Date.now(), publicKeys: publicKeys });
  }

  /**
   * Adds an authentication reference to the document.
   * @param authenticationReference to add to the document.
   */
  public addAuthenticationReference (authenticationReference: AuthenticationReference) {
    this.authenticationReferences.push(authenticationReference);
  }

  /**
   * Adds a service reference to the document.
   * @param serviceReference to add to the document.
   */
  public addServiceReference (serviceReference: ServiceReference) {
    this.serviceReferences.push(serviceReference);
  }
}
