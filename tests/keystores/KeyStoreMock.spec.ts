/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

// import { SignatureFormat } from '../../src/keystores/signatureFormat';
import UserAgentOptions from 'src/UserAgentOptions';
import { DidKey, KeyExport } from '@decentralized-identity/did-crypto-typescript';
import CryptoOptions from 'src/CryptoOptions';
import KeyStoreConstants from 'src/keystores/KeyStoreConstants';
import KeyStoreMock from 'tests/keystores/KeyStoreMock';
import { SignatureFormat } from 'src/keystores/SignatureFormat';

describe('KeyStoreMock', async () => {

  let options: UserAgentOptions;

  beforeAll(() => {
    options = {
      keyStore: new KeyStoreMock(),
      cryptoOptions: new CryptoOptions()
    };
  });

  it('should create a new RSA signature', async () => {
    // Setup registration environment
    await (<KeyStoreMock> options.keyStore).save(KeyStoreConstants.masterSeed, Buffer.from('xxxxxxxxxxxxxxxxx'));
    const didKey = new DidKey(
      (<CryptoOptions> options.cryptoOptions).cryptoApi,
      { name: 'RSASSA-PKCS1-v1_5', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]), hash: { name: 'SHA-256' } },
      null
    );
    const jwk: any = await didKey.getJwkKey(KeyExport.Private);
    const keyStore = <KeyStoreMock> options.keyStore;
    await keyStore.save('key', jwk);
    const signature = await keyStore.sign('key', 'abc', SignatureFormat.FlatJsonJws);
    expect(signature).toBeDefined();
  });
});
