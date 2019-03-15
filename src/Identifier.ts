/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import DidKey from '../../did-common-typescript/lib/crypto/DidKey';
import { KeyType } from '../../did-common-typescript/lib/crypto/KeyType';
import { KeyUse } from '../../did-common-typescript/lib/crypto/KeyUse';
// import { DidKey, KeyType, KeyUse } from '@decentralized-identity/did-common-typescript';
import { PublicKey } from './types';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';
import KeyStore from './keystores/KeyStore';
import { KeyExport } from '../../did-common-typescript/lib/crypto/KeyExport';
import KeyStoreConstants from './keystores/KeyStoreConstants';

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 */
export default class Identifier {
  /**
   * The string representation of the identier
   * in the format 'did:{method}:{id}'.
   */
  public id: string;

  /**
   * The identifier document for the identifier
   * if one exists.
   */
  public document: IdentifierDocument | undefined;

  /**
   * Constructs an instance of the Identifier
   * class using the provided identifier or identifier document.
   * @param identifier either the string representation of an identifier or a identifier document.
   * @param [options] for configuring how to register and resolve identifiers.
   */
  constructor (public identifier: IdentifierDocument | string, private options?: UserAgentOptions) {
    // Check whether passed an identifier document
    // or an identifier string
    if (typeof identifier === 'object') {
      this.document = identifier;
      this.id = identifier.id;
    } else {
      this.id = identifier;
    }
  }

  /**
   * Creates a new decentralized identifier.
   * @param options for configuring how to register and resolve identifiers.
   */
/*   public static async create (options: UserAgentOptions): Promise<Identifier> {
    throw new Error('Not implemented');
  } */

  /**
   * Creates a new decentralized identifier, using the current identifier
   * and the specified target. If the registar flag is true, the newly created
   * identifier will be registered using the
   * @param crypto Web crypto object
   * @param alg Web crypto compliant algorithm object
   * @param personaId Identifier for the persona for whom we create the decentralized identifier
   * @param target entity for which to create the linked identifier
   * @param register flag indicating whether the new identifier should be registered
   * with a ledger.
   */
  public async createLinkedIdentifier (
    crypto: any, alg: any, personaId: string, target: string, options: UserAgentOptions, register: boolean = false): Promise<IdentifierDocument> {
    if (options.keyStore) {
      let keyStore: KeyStore = options.keyStore;
      let key: DidKey;
      return keyStore.get(KeyStoreConstants.masterSeed)
      .then((seed: Buffer) => {
        // Create DID key
        let didKey: any = new DidKey(crypto, alg, KeyType.EC, KeyUse.Signature, null);
        return didKey.generatePairwise(seed, personaId, target);
      })
     .then((pairwiseKey: DidKey) => {
       key = pairwiseKey;
        // TODO add key type in the storage identfier
       return keyStore.save(this.pairwiseKeyStorageIdentifier(personaId, target), pairwiseKey);
     })
     .then((success: boolean) => {
       if (success) {
         if (register) {
           throw new Error('Not implemented');
         }

         return this.createIdentifierDocument(key);
       } else {
         let message = `Error while saving pairwise key for DID '${personaId}' to key store.`;
         throw new UserAgentError(message);
       }
     })
     .catch((err) => {
       throw new UserAgentError(`Error occured: '${err}'`);
     });
    } else {
      throw new UserAgentError('No KeyStore specified in options');
    }
  }

  /**
   * Gets the IdentifierDocument for the identifier
   * instance, throwing if no identifier has been
   * created.
   */
  public async getDocument (): Promise <IdentifierDocument> {
    // If we already have not already
    // retrieved the document use the
    // resolver to get the document
    if (!this .document) {
      if (!this.options || !this.options.resolver) {
        throw new UserAgentError('Resolver not specified in user agent options.');
      }

      // We need to resolve the document
      this.document = await this.options.resolver.resolve(this);
    }

    return this.document;
  }

  /**
   * Performs a public key lookup using the
   * specified key identifier, returning the
   * key defined in document.
   * @param keyIdentifier the identifier of the public key.
   */
  public async getPublicKey (keyIdentifier ?: string): Promise <PublicKey> {
    if (!this .document) {
      await this.getDocument();
    }

    // If we have been provided a key identifier use
    // the identifier to look up a key in the document
    if (this.document && this.document.publicKeys && keyIdentifier) {
      const index = this.document.publicKeys.findIndex(key => key.id === keyIdentifier);
      if (index === -1) {
        throw new UserAgentError(`Document does not contain a key with id '${keyIdentifier}'`);
      }

      return this.document.publicKeys[index];
    } else if (this.document && this.document.publicKeys && this.document.publicKeys.length > 0) {
      // If only one key has been specified in the document
      // return that
      return this.document.publicKeys[0];
    }

    throw new UserAgentError('Document does not contain any public keys');
  }

  private createIdentifierDocument (key: DidKey): Promise <IdentifierDocument> {
    return this.getDidPublicKey(key)
    .then((publicKeyJwk) => {
      return new IdentifierDocument({ id: this.id, created: Date.now(), publicKey: [ publicKeyJwk ] });
    });
  }

  private getDidPublicKey (key: DidKey): Promise <any> {
    return key.getJwkKey(KeyExport.Public)
    .then((jwk) => {
      return jwk;
    })
    .catch((err) => {
      throw new UserAgentError(`Could not retrieve public key for pairwise did. Failure '${err}'`);
    });
  }

  private pairwiseKeyStorageIdentifier (personaId: string, target: string): string {
    return `${personaId}-${target}`;
  }
}
