/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, PublicKey } from './types';
import base64url from 'base64url';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';
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
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * The id is generated.
   * @param idBase The base id in format did:{method}:{id}. {id} will be filled in by this method
   * @param publicKeys to include in the document.
   * @param options User agent options containing the crypto Api
   */
  public static async createAndGenerateId (idBase: string, publicKeys: Array<PublicKey>, options: UserAgentOptions): Promise<IdentifierDocument> {
    let document = IdentifierDocument.create(idBase, publicKeys);
    let id: string = await IdentifierDocument.createIdOnDocument(document, options);
    document.id = id;
    return document;
  }

  /**
   * Create an identifier on the document.
   * If the document has an identifier already, this is firstly removed.
   * @param document The document on which to caluclate the identifier
   * @param options User agent options containing the crypto Api
   */
  public static async createIdOnDocument (document: IdentifierDocument, options: UserAgentOptions): Promise<string> {
    // Strip id
    let did = document.id;
    delete document.id;

    // Encode document
    let serialized = JSON.stringify(document);
    let encoded = base64url(serialized);
    let toHash: ArrayBuffer = IdentifierDocument.string2ArrayBuffer(encoded);

    // calculate identifier
    let id = await options.cryptoOptions!.cryptoApi.subtle.digest({ name: 'SHA-256' }, toHash);
    let buf: Buffer = Buffer.from(id);
    let idDid = base64url(buf);
    let didComponents = did.split(':');
    if (didComponents.length < 2) {
      throw new UserAgentError(`Invalid did '${did}' passed. Should have at least did:<method>.`);
    }
    return `${didComponents[0]}:${didComponents[1]}:${idDid}`;
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
      clonedDocument.publicKeys = undefined;
    }

    // Now return the cloned document for serialization
    return clonedDocument;
  }

  private static string2ArrayBuffer (text: string): ArrayBuffer {
    let buf = new ArrayBuffer(text.length);
    let bufView = new Uint8Array(buf);
    let strLen = text.length;
    for (let inx = 0; inx < strLen; inx++) {
      bufView[inx] = text.charCodeAt(inx);
    }
    return buf;
  }
}
