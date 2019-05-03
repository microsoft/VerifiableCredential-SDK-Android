/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../src/Identifier';
import IdentifierDocument from '../src/IdentifierDocument';
import UserAgentOptions from '../src/UserAgentOptions';
import KeyStoreMock from './keystores/KeyStoreMock';
import TestResolver from './TestResolver';
import CryptoOptions from '../src/CryptoOptions';
import { KeyTypeFactory } from '@decentralized-identity/did-crypto-typescript';
import SidetreeRegistrar from '../src/registrars/SidetreeRegistrar';
import KeyStoreConstants from '../src/keystores/KeyStoreConstants';
import IRegistrar from '../src/registrars/IRegistrar';

interface CreateIdentifier {
  (options: UserAgentOptions, identifier: Identifier, register: boolean): Promise<Identifier>;
}

class Helpers {
  /**
   * Create identifier helper
   * @param register True if registration is requested
   * @param testResolver The resolver
   * @param keyStore The key store
   * @param alg The algorithm to use
   * @param create Delegate for the creation of the identifer
   */
  public static async testIdentifier (register: boolean, testResolver: any, keyStore: KeyStoreMock, alg: any, create: CreateIdentifier) {
    const options = <UserAgentOptions> {
      resolver: testResolver,
      timeoutInSeconds: 30,
      keyStore: keyStore,
      cryptoOptions: new CryptoOptions(),
      didPrefix: 'did:ion'
    };

    const registar: IRegistrar = new SidetreeRegistrar('beta.ion.microsoft.com', options);
    options.registrar = registar;

    const personaId = 'did:ion:identifier';
    options.cryptoOptions!.algorithm = alg;
    let identifier = new Identifier(personaId, options);
    expect(personaId).toBe(<string> identifier.identifier);

    identifier = await create(options, identifier, register);
    expect(identifier.id).toBeDefined();
    expect<Boolean>(identifier.id.startsWith('did:ion:')).toBe(true);
    const id = identifier.id;
    const kty = KeyTypeFactory.create(alg);
    console.log(`Identifier: Test key type ${kty}`);
    expect(kty).toBe((identifier.document!.publicKeys[0].publicKeyJwk).kty);

    const document: Identifier = new Identifier(<IdentifierDocument> identifier.document);
    expect(id).toBe(document.id);
    expect(id).toBe((<IdentifierDocument> document.identifier).id);

    expect(id).toBe(document.document!.id);
    expect(identifier.document!.publicKeys).toBe(document.document!.publicKeys);
  }
}

describe('Pairwise Identifier', async () => {
  let testResolver: TestResolver;
  let options: UserAgentOptions;
  // Set key store and its data
  const keyStore: KeyStoreMock | undefined = undefined;

  beforeAll(async () => {
    // Configure the agent options for the tests
    options = <UserAgentOptions> {
      resolver: testResolver,
      timeoutInSeconds: 30,
      keyStore: keyStore,
      cryptoOptions: new CryptoOptions(),
      didPrefix: 'did:ion'
    };
    testResolver = new TestResolver();
    options.keyStore = new KeyStoreMock();
    await (<KeyStoreMock> options.keyStore).save(KeyStoreConstants.masterSeed, Buffer.from('my master seed'));
    options.registrar = new SidetreeRegistrar('https://example.com', options);
  });

  it('should throw when creating a linked identifier and no key store specified in user agent options', async () => {
    const personaId = 'did:ion:identifier';
    const identifier = new Identifier(personaId, {});
    let throwDetected: boolean = false;
    await identifier.createLinkedIdentifier('did:ion:peer')
    .catch((err: any) => {
      expect('No keyStore in options').toBe(err.message);
      throwDetected = true;
    });

    if (!throwDetected) {
      fail('No Throw detected');
    }
  });

  it('should throw when creating a linked identifier and no registrar specified in user agent options', async () => {
    const personaId = 'did:ion:identifier';
    (<CryptoOptions> options.cryptoOptions).algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    delete options.registrar;
    const identifier = new Identifier(personaId, options);
    let throwDetected: boolean = false;
    await identifier.createLinkedIdentifier('did:ion:peer', true)
    .catch((err: any) => {
      expect('No registrar in options to register DID document').toBe(err.message);
      throwDetected = true;
    });

    if (!throwDetected) {
      fail('No Throw detected');
    }
  });

  const alg = [
    { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
    { name: 'RSASSA-PKCS1-v1_5', hash: { name: 'SHA-256' } }
  ];

  it('should create an identifier EC', async () => {

    /* tslint:disable:max-line-length */
    await Helpers
    .testIdentifier(false, testResolver, <KeyStoreMock> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });
  });

/*
  Not yet supported in sidetree core
  fit('should create an identifier RSA', async done => {
    await Helpers
    .testIdentifier(false, testResolver, options.keyStore as KeyStoreMock, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });

    done();
  });
*/
  it('should create a pairwise identifier EC', async () => {
    await Helpers
    .testIdentifier(false, testResolver, <KeyStoreMock> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${options}-${register}`);
      return identifier.createLinkedIdentifier('did:ion:peer', register);
    });
  });

  it('should create a pairwise identifier RSA', async () => {
    await Helpers.testIdentifier(
      false, testResolver, <KeyStoreMock> options.keyStore, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(`${options}-${register}`);
        return identifier.createLinkedIdentifier('did:ion:peer', register);
      });
  });

  it('should create an identifier and register for EC', async () => {

    await Helpers.testIdentifier(
      true, testResolver, <KeyStoreMock> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(options);
        return identifier.createLinkedIdentifier('did:ion:peerforregister', register);
      });
  });
/*
  Not yet supported in sidetree core
  fit('should create an identifier and register for RSA', async done => {
    await Helpers.testIdentifier(
      true, testResolver, options.keyStore as KeyStoreMock, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(options);
        return identifier.createLinkedIdentifier('did:ion:peer', register);
      });
    done();
  });
*/
});
