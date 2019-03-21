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
import CryptoOptions from '../src/CryptoOptions';
// import DidAuth from '@decentralized-identity/did-auth-jose';

describe('Pairwise Identifier', () => {
  const testResolver = new TestResolver();


  it('Test throws - no keystore', async done => {
    const personaId = 'did:test:identifier';
    const identifier = new Identifier(personaId, {});
    let throwDetected: boolean = false;
    await identifier.createLinkedIdentifier('did:test:peer', false)
    .catch ((err) => {
      expect('No keyStore in options').toBe(err.message);
      throwDetected = true;
    });

    if (!throwDetected) {
      fail('No Throw detected');
    }
    done();
  });

  fit('Test throws - storage failure', async done => {
    const personaId = 'identifier to simulate storage failure';
    options.cryptoOptions!.algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    const identifier = new Identifier(personaId, options);
    let throwDetected: boolean = false;
    await identifier.createLinkedIdentifier('did:test:peer', false)
    .catch ((err) => {
      expect(`Error while saving pairwise key for DID 'identifier to simulate storage failure' to key store.`).toBe(err.message);
      throwDetected = true;
    });
  
    if (!throwDetected) {
      fail('No Throw detected');
    }
    done();
  });

  it('Test throws - bad id format', async done => {
    const personaId = 'identifier';
    options.cryptoOptions!.algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    const identifier = new Identifier(personaId, options);
    let throwDetected: boolean = false;
    await identifier.createLinkedIdentifier('did:test:peer', false)
    .catch ((err) => {
      expect(`Invalid did 'identifier' passed. Should have at least did:<method>.`).toBe(err.message);
      throwDetected = true;
    });
  
    if (!throwDetected) {
      fail('No Throw detected');
    }
    done();
  });
    
    // Set key store and its data
    let keyStore: KeyStoreMock = new KeyStoreMock();
    keyStore.save(KeyStoreConstants.masterSeed, Buffer.from('my master seed'));
  
    // Configure the agent options for the tests
    const options = {
      resolver: testResolver,
      timeoutInSeconds: 30,
      keyStore: keyStore,
      cryptoOptions: new CryptoOptions()
    } as UserAgentOptions;
  
    it('create an EC identifier', async done => {
      const personaId = 'did:test:identifier';
      const identifier = new Identifier(personaId, options);
      options.cryptoOptions!.algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
      expect(personaId).toBe(identifier.identifier as string);
  
      let identifierDoc: IdentifierDocument = await identifier.create(false);
      expect(identifierDoc.id).toBeDefined();
      expect<Boolean>(identifierDoc.id.startsWith('did:test:')).toBe(true);
      let id = identifierDoc.id;
      expect('EC').toBe((identifierDoc.publicKeys[0] as any).kty);
  
      let document: Identifier = new Identifier(identifierDoc);
      expect(id).toBe(document.id);
      expect(id).toBe((document.identifier as IdentifierDocument).id);
  
      expect(id).toBe(document!.document!.id);
      expect(identifierDoc.publicKeys).toBe(document!.document!.publicKeys);
      done();
    });
  
  it('create an EC pairwise identifier', async done => {
    const personaId = 'did:test:identifier';
    const identifier = new Identifier(personaId, options);
    options.cryptoOptions!.algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    expect(personaId).toBe(identifier.identifier as string);

    let identifierDoc: IdentifierDocument = await identifier.createLinkedIdentifier('did:test:peer', false);
    expect(identifierDoc.id).toBeDefined();
    expect<Boolean>(identifierDoc.id.startsWith('did:test:')).toBe(true);
    let id = identifierDoc.id;
    expect('EC').toBe((identifierDoc.publicKeys[0] as any).kty);

    let document: Identifier = new Identifier(identifierDoc);
    expect(id).toBe(document.id);
    expect(id).toBe((document.identifier as IdentifierDocument).id);

    expect(id).toBe(document!.document!.id);
    expect(identifierDoc.publicKeys).toBe(document!.document!.publicKeys);
    done();
  });
});
