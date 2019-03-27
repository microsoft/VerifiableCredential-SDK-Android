/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../src/Identifier';
import IdentifierDocument from '../src/IdentifierDocument';
import UserAgentOptions from '../src/UserAgentOptions';
import InMemoryKeyStore from '../src/keystores/InMemoryKeyStore';
import TestResolver from './TestResolver';
import KeyStoreConstants from '../src/keystores/KeyStoreConstants';
import CryptoOptions from '../src/CryptoOptions';
import { KeyTypeFactory } from '@decentralized-identity/did-common-typescript'
import Registrar from '../src/registrars/IRegistrar';
import SidetreeRegistrar from '../src/registrars/SidetreeRegistrar';

interface CreateIdentifier {
  (options: UserAgentOptions, identifier: Identifier, register: boolean): Promise<Identifier>;
}

class Helpers {
  public static async testIdentifier(register: boolean, testResolver: any, keyStore: InMemoryKeyStore, alg: any, create: CreateIdentifier) {
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
    let identifier = new Identifier(personaId, options);
    expect(personaId).toBe(identifier.identifier as string);

    identifier = await create(options, identifier, register);
    expect(identifier.id).toBeDefined();
    expect(personaId).toBe(identifier.identifier as string);
    expect<Boolean>(identifier.id.startsWith('did:test:')).toBe(true);
    const id = identifier.id;
    const kty = KeyTypeFactory.create(alg);
    console.log(`Identifier: Test key type ${kty}`);
    expect(kty).toBe((identifier!.document!.publicKeys[0] as any).kty);

    const document: Identifier = new Identifier(identifier.document as IdentifierDocument);
    expect(id).toBe(document.id);
    expect(id).toBe((document.identifier as IdentifierDocument).id);

    expect(id).toBe(document!.document!.id);
    expect(identifier!.document!.publicKeys).toBe(document!.document!.publicKeys);
  }
}

describe('Pairwise Identifier', async () => {
  const testResolver = new TestResolver();
  // Set key store and its data
  const keyStore: InMemoryKeyStore = new InMemoryKeyStore();
  await keyStore.save(KeyStoreConstants.masterSeed, Buffer.from('my master seed'));

  // Configure the agent options for the tests
  const options = {
    resolver: testResolver,
    timeoutInSeconds: 30,
    keyStore: keyStore,
    cryptoOptions: new CryptoOptions()
  } as UserAgentOptions;

  fit('should throw when creating a linked identifier and no key store specified in user agent options', async done => {
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

  it('should throw when creating a linked identifier with a bad formatted personaId', async done => {
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

  it('should create an identifier', async done => {
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });
    done();
  });
  
  it('should create a pairwise identifier', async done => {
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${options}-${register}`);
      return identifier.createLinkedIdentifier('did:test:peer', register);
    });
    await Helpers.testIdentifier(false, testResolver, keyStore, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${options}-${register}`);
      return identifier.createLinkedIdentifier('did:test:peer', register);
    });
    console.log(`Pairwise done`);
    done();
  });
});
