/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, PublicKey } from './types';
import UserAgentOptions from './UserAgentOptions';
import { Identifier } from '.';
const cloneDeep = require('lodash/fp/cloneDeep');

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
    this.publicKeys = document.publicKeys;

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
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * The id is generated.
   * @param idBase The base id in format did:{method}:{id}. {id} will be filled in by this method
   * @param publicKeys to include in the document.
   * @param options User agent options containing the crypto Api
   */
  public static async createAndGenerateId (idBase: string, publicKeys: Array<PublicKey>, options: UserAgentOptions): Promise<IdentifierDocument> {
    const document = IdentifierDocument.create(idBase, publicKeys);
    const identifier: Identifier = await options.registrar!.generateIdentifier(document);
    document.id = identifier.id;
    return document;
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

  /**
   * Used to control the the properties that are
   * output by JSON.stringify.
   */
  public toJSON (): any {
    // Clone the current instance. Note the use of
    // a deep clone to ensure immutability of
    // the instance being cloned for serialization
    const clonedDocument = cloneDeep(this);

    // Add the JSON-LD context
    clonedDocument['@context'] = 'https://w3id.org/did/v1';

    if (!this.authenticationReferences || this.authenticationReferences.length === 0) {
      clonedDocument.authenticationReferences = undefined;
    }

    if (!this.serviceReferences || this.serviceReferences.length === 0) {
      clonedDocument.serviceReferences = undefined;
    }

    if (!this.publicKeys || this.publicKeys.length === 0) {
      clonedDocument.publicKey = undefined;
    }

    // Now return the cloned document for serialization
    return clonedDocument;
  }
}
