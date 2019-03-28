
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IKeyStore from './IKeyStore';
import PouchDB from 'pouchdb';
import PouchDBAdapterMemory from 'pouchdb-adapter-memory';
import UserAgentError from '../UserAgentError';
import { DidKey } from '@decentralized-identity/did-common-typescript';
PouchDB.plugin(PouchDBAdapterMemory);
const keyStore = new PouchDB('keyStore', { adapter: 'memory' });

/**
 * An encrypted in memory implementation of IKeyStore using PouchDB
 * and memdown. As soon as the process ends or
 * the reference to the store is released all data is discarded.
 *
 * This implementation is intended as a batteries included approach
 * to allow simple testing and experimentation with the UserAgent SDK.
 */
export default class InMemoryKeyStore implements IKeyStore {

  /**
   * Constructs an instance of the in memory key store
   * optionally encrypting the contents of the store
   * using the specified encryption key.
   * @param [encryptionKey] a 32 byte buffer that will
   * be used as the key or a string which will be used to
   * generate one.
   */
  constructor (encryptionKey?: Buffer | string) {
    if (encryptionKey) {

      const options: any = {};
      const isBuffer = encryptionKey instanceof Buffer;

      // If passed a buffer check that the
      // size is 32 bytes, otherwise throw
      if (isBuffer && encryptionKey.length !== 32) {
        throw new UserAgentError('The encryption key buffer must be 32 bytes.');
      }

      if (isBuffer) {
        options.key = encryptionKey;
      } else {
        options.password = encryptionKey;
      }

      PouchDB.plugin(require('crypto-pouch'));

      // Set the encryption key for the store
      (keyStore as any).crypto(options);
    }
  }

  /**
   * Gets the key from the store using the specified identifier.
   * @param keyIdentifier for which to return the key.
   */
  public async get (keyIdentifier: string): Promise<Buffer | DidKey> {
    try {
      const keyDocument: any = await keyStore.get(keyIdentifier);
      return Buffer.from(keyDocument.key);
    } catch (error) {
      throw new UserAgentError(`No key found for '${keyIdentifier}'.`);
    }
  }

  /**
   * Saves the specified key to the store using the key identifier.
   * @param keyIdentifier to store the key against
   * @param key the key to store.
   */
  public async save (keyIdentifier: string, key: Buffer | DidKey): Promise<void> {
    // Format the document
    const keyDocument = {
      _id: keyIdentifier,
      key: key
    };

    await keyStore.put(keyDocument);
  }
}
