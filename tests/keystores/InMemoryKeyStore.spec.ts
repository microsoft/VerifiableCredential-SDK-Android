/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import InMemoryKeyStore from '../../src/keystores/InMemoryKeyStore';
import { SignatureFormat } from '../../src/keystores/SignatureFormat';

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

  it('should throw when key not found', async () => {
    try {
      const keyStore = new InMemoryKeyStore();
      await keyStore.getKey('does not exist');
    } catch (error) {
      expect(error.message).toEqual(`No key found for 'does not exist'.`);
    }
  });

  it('should save key to store and retrieve saved key', async () => {
    try {
      const keyBuffer: Buffer = Buffer.from('Some key material');
      const keyStore = new InMemoryKeyStore();
      await keyStore.save('did:ion:123456789#master', keyBuffer);

      // Now try get get the key back
      const buffer: Buffer = <Buffer> await keyStore.getKey('did:ion:123456789#master');
      expect(buffer).toBeDefined();
      expect(buffer.toString()).toEqual('Some key material');
    } catch (error) {
      fail(`Exception not expected, got: '${error}'`);
    }
  });

  it('should save object as key to store and retrieve saved key', async () => {
    try {
      const keyObject: any = {
        kty: 'EC',
        use: 'sig'
      };
      const keyStore = new InMemoryKeyStore();
      await keyStore.save('did:ion:abcdef', keyObject);

      // Now try get get the key back
      const key: any = await keyStore.getKey('did:ion:abcdef');
      expect(key).toBeDefined();
      expect('EC').toEqual(key.kty);
      expect('sig').toEqual(key.use);
    } catch (error) {
      fail(`Exception not expected, got: '${error}'`);
    }
  });

  it('should save key to store and retrieve saved key when using encrypted store', async () => {
    try {
      const keyBuffer: Buffer = Buffer.from('Some key material');
      const keyStore = new InMemoryKeyStore('password');
      await keyStore.save('did:ion:987654321#master', keyBuffer);

      // Now try get get the key back
      const buffer: Buffer = <Buffer> await keyStore.getKey('did:ion:987654321#master');
      expect(buffer).toBeDefined();
      expect(buffer.toString()).toEqual('Some key material');
    } catch (error) {
      fail(`Exception not expected, got: '${error}'`);
    }
  });

  it('should throw because of missing key reference', async (done) => {
    let throwCaught = false;
    const keyStore = new InMemoryKeyStore();
    await keyStore.sign('key', 'abc', SignatureFormat.FlatJsonJws)
    .then(() => {
      if (!throwCaught) {
        fail('No throw detected because of missing key in store');
      }
    })
    .catch((err) => {
      throwCaught = true;
      expect(`The key referenced by 'key' is not available: 'Error: No key found for 'key'.'`).toBe(err.message);
      done();
    });
  });

  it('should throw because of bad signature format', async (done) => {
    let throwCaught = false;
    const keyStore = new InMemoryKeyStore();
    const keyStoreCopy: any = keyStore;
    await keyStoreCopy.sign('key', 'abc', -1)
    .then(() => {
      if (!throwCaught) {
        fail('No throw detected because of missing key in store');
      }
    })
    .catch((err: any) => {
      throwCaught = true;
      expect(`The signature format '-1' is not supported`).toBe(err.message);
      done();
    });
  });
});
