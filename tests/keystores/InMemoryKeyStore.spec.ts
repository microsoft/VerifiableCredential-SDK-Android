/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import InMemoryKeyStore from '../../src/keystores/InMemoryKeyStore';

describe('InMemoryKeyStore', () => {
  it('should return a new instance with no encryption', () => {
    const keyStore = new InMemoryKeyStore();
    expect(keyStore).toBeDefined();
  });

  it('should return a new instance with encryption enabled via string', () => {
    const keyStore = new InMemoryKeyStore('some password string');
    expect(keyStore).toBeDefined();
  });

  it('should return a new instance with encryption enabled via buffer', () => {
    const encryptionKey = Buffer.from('7468697320697320612074c3a9737445');
    const keyStore = new InMemoryKeyStore(encryptionKey);
    expect(keyStore).toBeDefined();
  });

  it('should throw when encryption key buffer less than 32 bytes', () => {
    const encryptionKey = Buffer.from('8697320697'); // 10
    expect(() => new InMemoryKeyStore(encryptionKey)).toThrowError('The encryption key buffer must be 32 bytes.');
  });

  it('should throw when encryption key buffer greater than 32 bytes', () => {
    const encryptionKey = Buffer.from('7468697320697320612074c3a973748697320697'); // 40
    expect(() => new InMemoryKeyStore(encryptionKey)).toThrowError('The encryption key buffer must be 32 bytes.');
  });

  it('should throw when key not found', async (done) => {
    try {
      const keyStore = new InMemoryKeyStore();
      await keyStore.get('does not exist');
    } catch (error) {
      expect(error.message).toEqual(`No key found for 'does not exist'.`);
    }
    done();
  });

  it('should save key to store and retrieve saved key', async (done) => {
    try {
      const keyBuffer: Buffer = Buffer.from('Some key material');
      const keyStore = new InMemoryKeyStore();
      await keyStore.save('did:ion:123456789#master', keyBuffer);

      // Now try get get the key back
      const buffer: Buffer = await keyStore.get('did:ion:123456789#master') as Buffer;
      expect(buffer).toBeDefined();
      expect(buffer.toString()).toEqual('Some key material');
    } catch (error) {
      fail(`Exception not expected, got: '${error}'`);
    }
    done();
  });

  it('should save key to store and retrieve saved key when using encrypted store', async (done) => {
    try {
      const keyBuffer: Buffer = Buffer.from('Some key material');
      const keyStore = new InMemoryKeyStore('password');
      await keyStore.save('did:ion:987654321#master', keyBuffer);

      // Now try get get the key back
      const buffer: Buffer = await keyStore.get('did:ion:987654321#master') as Buffer;
      expect(buffer).toBeDefined();
      expect(buffer.toString()).toEqual('Some key material');
    } catch (error) {
      fail(`Exception not expected, got: '${error}'`);
    }
    done();
  });
});
