/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../src/Identifier';
import IdentifierDocument from '../src/IdentifierDocument';
import UserAgentOptions from '../src/UserAgentOptions';
import KeyStoreMock from '../src/keystores/KeyStoreMock';
import TestResolver from './TestResolver';
import KeyStoreConstants from '../src/keystores/KeyStoreConstants';
import WebCrypto from 'node-webcrypto-ossl';
const crypto = new WebCrypto();

describe('Pairwise Identifier', () => {
  const testResolver = new TestResolver();

  // Set key store and its data
  let keyStore: KeyStoreMock = new KeyStoreMock();
  keyStore.save(KeyStoreConstants.masterSeed, Buffer.from('my master seed'));

  // Configure the agent options for the tests
  const options = {
    resolver: testResolver,
    timeoutInSeconds: 30,
    keyStore: keyStore 
  } as UserAgentOptions;

  it('create an EC paiwise identifier', async done => {
    const personaId = 'did:test:identifier';
    const identifier = new Identifier(personaId, options);
    const alg = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    identifier.createLinkedIdentifier(crypto, alg, 'my persona', 'peer', options, false)
    .then((identifierDoc: IdentifierDocument) => {
      expect(personaId).toBe(identifierDoc.id);
      expect('EC').toBe((identifierDoc.publicKeys[0] as any).kty);
      done();
    })
  });
});
