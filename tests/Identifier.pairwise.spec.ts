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
import { KeyTypeFactory } from '@decentralized-identity/did-common-typescript'
import Registrar from '../src/registrars/IRegistrar';
import SidetreeRegistrar from '../src/registrars/SidetreeRegistrar';

interface CreateIdentifier {
  (identifier: Identifier, register: boolean): Promise<IdentifierDocument>;
}

class Helpers {
  public static async testIdentifier(register: boolean, testResolver: any, keyStore: KeyStoreMock, alg: any, create: CreateIdentifier) {
    const options = {
      resolver: testResolver,
      timeoutInSeconds: 30,
      keyStore: keyStore,
      cryptoOptions: new CryptoOptions()
    } as UserAgentOptions;

    let registar: Registrar | undefined;
    if (register) {
      registar = new SidetreeRegistrar('https://beta.register.did.microsoft.com/api/v1.1', options);
      options.registrar = registar;
    }

    const personaId = 'did:test:identifier';
    options.cryptoOptions!.algorithm = alg;
    const identifier = new Identifier(personaId, options);
    expect(personaId).toBe(identifier.identifier as string);

    let identifierDoc: IdentifierDocument = await create(identifier, register);
    expect(identifierDoc.id).toBeDefined();
    expect<Boolean>(identifierDoc.id.startsWith('did:test:')).toBe(true);
    let id = identifierDoc.id;
    let kty = KeyTypeFactory.create(alg);
    console.log(`Identifier: Test key type ${kty}`);
    expect(kty).toBe((identifierDoc.publicKeys[0] as any).kty);

    let document: Identifier = new Identifier(identifierDoc);
    expect(id).toBe(document.id);
    expect(id).toBe((document.identifier as IdentifierDocument).id);

    expect(id).toBe(document!.document!.id);
    expect(identifierDoc.publicKeys).toBe(document!.document!.publicKeys);
  }
  
}

describe('Pairwise Identifier', () => {
  const testResolver = new TestResolver();
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
  
    const alg = [
      { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
      { name: 'RSASSA-PKCS1-v1_5', hash: { name: 'SHA-256' } }
    ];

    it('create an identifier', async done => {
      await Helpers.testIdentifier(false, testResolver, keyStore, alg[0], async (identifier: Identifier, register: boolean) => {
        return identifier.create(register);
      });
      await Helpers.testIdentifier(false, testResolver, keyStore, alg[1], async (identifier: Identifier, register: boolean) => {
        return identifier.create(register);
      });
      done();
    });
  
  it('create a pairwise identifier', async done => {
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[0], async (identifier: Identifier, register: boolean) => {
      return identifier.createLinkedIdentifier('did:test:peer', register);
    });
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[1], async (identifier: Identifier, register: boolean) => {
      return identifier.createLinkedIdentifier('did:test:peer', register);
    });
    console.log(`Pairwise done`);
    done();
  });
});
