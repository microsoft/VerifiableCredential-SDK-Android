/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, PublicKey } from './types';

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
  public created: Date | undefined;

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
    this.publicKeys = document.publicKey;

    if (document.created) {
      this.created = new Date(document.created);
    }

    this.authenticationReferences = document.authentication || new Array();
    this.serviceReferences = document.service || new Array();
  }

  /**
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * @param publicKeys to include in the document.
   */
  public static create (id: string, publicKeys: Array<PublicKey>): IdentifierDocument {
    const createdDate = new Date(Date.now()).toISOString();
    return new IdentifierDocument({ id: id, created: createdDate, publicKey: publicKeys });
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
