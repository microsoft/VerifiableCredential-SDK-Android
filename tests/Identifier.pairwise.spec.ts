/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../src/Identifier';
import IdentifierDocument from '../src/IdentifierDocument';
import UserAgentOptions from '../src/UserAgentOptions';
import TestResolver from './resolvers/TestResolver';
import SidetreeRegistrar from '../src/registrars/SidetreeRegistrar';
import KeyStoreConstants from '../src/keystores/KeyStoreConstants';
import IRegistrar from '../src/registrars/IRegistrar';
import KeyStoreInMemory from '../src/crypto/keyStore/KeyStoreInMemory';
import KeyTypeFactory, { KeyType } from '../src/crypto/keys/KeyTypeFactory';
import CryptoHelpers from '../src/crypto/utilities/CryptoHelpers';
import SecretKey from '../src/crypto/keys/SecretKey';

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
  public static async testIdentifier (register: boolean, testResolver: any, keyStore: KeyStoreInMemory, alg: any, create: CreateIdentifier) {
    const options = new UserAgentOptions();
    options.resolver = testResolver;
    options.timeoutInSeconds = 30;
    options.keyStore = keyStore;
    options.didPrefix = 'did:ion-test';

    const registrar: IRegistrar = new SidetreeRegistrar('https://beta.ion.microsoft.com/api/1.0/register', options);
    options.registrar = registrar;

    const personaId = 'did:ion-test:identifier';
    options.cryptoOptions!.authenticationSigningJoseAlgorithm = CryptoHelpers.webCryptoToJwa(alg);
    let identifier = new Identifier(personaId, options);
    expect(personaId).toBe(<string> identifier.identifier);

    identifier = await create(options, identifier, register);
    expect(identifier.id).toBeDefined();
    expect<Boolean>(identifier.id.startsWith('did:ion-test:')).toBe(true);
    const id = identifier.id;
    const kty = KeyTypeFactory.createViaWebCrypto(alg);
    console.log(`Identifier: Test key type ${kty}`);
    expect(kty).toBe(<KeyType>(identifier.document!.getPublicKeysFromDocument()[0]).kty);

    const document: Identifier = new Identifier(<IdentifierDocument> identifier.document);
    expect(id).toBe(document.id);
    expect(id).toBe((<IdentifierDocument> document.identifier).id);

    expect(id).toBe(document.document!.id);
    expect(identifier.document!.publicKeys).toBe(document.document!.publicKeys);
  }
}

describe('Pairwise Identifier', () => {
  let testResolver: TestResolver;
  let options: UserAgentOptions;

  beforeAll(async () => {
    // Configure the agent options for the tests
    options = new UserAgentOptions();
    options.resolver = testResolver;
    options.timeoutInSeconds = 30;
    options.didPrefix = 'did:ion';
    testResolver = new TestResolver();
    options.keyStore = new KeyStoreInMemory();
    const seed = new SecretKey('ABDE');
    await options.keyStore.save(KeyStoreConstants.masterSeed, seed);
    options.registrar = new SidetreeRegistrar('https://example.com', options);
  });

  it('should throw when creating a linked identifier and no key store specified in user agent options', async () => {
    const personaId = 'did:ion:identifier';
    const identifier = new Identifier(personaId, <any>{});
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
    options.cryptoOptions.authenticationSigningJoseAlgorithm = 'ES256K';
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

  // tslint:disable-next-line:mocha-no-side-effect-code
  it('should create an identifier EC', async () => {

    /* tslint:disable:max-line-length */
    await Helpers
    .testIdentifier(false, testResolver, <KeyStoreInMemory> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });
  });

/*
  Not yet supported in sidetree core
  it('should create an identifier RSA', async done => {
    await Helpers
    .testIdentifier(false, testResolver, options.keyStore as KeyStoreInMemory, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${identifier}-${register}`);
      return Identifier.create(options);
    });

    done();
  });
*/
  it('should create a pairwise identifier EC', async () => {
    options.didPrefix = 'did:ion';
    await Helpers
    .testIdentifier(false, testResolver, <KeyStoreInMemory> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
      console.log(`${options}-${register}`);
      return identifier.createLinkedIdentifier('did:ion:peer', register);
    });
  });

  it('should create a pairwise identifier RSA', async () => {
    options.didPrefix = 'did:ion';
    await Helpers.testIdentifier(
      false, testResolver, <KeyStoreInMemory> options.keyStore, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(`${options}-${register}`);
        return identifier.createLinkedIdentifier('did:ion:peer', register);
      });
  });

  it('should create an identifier and register for EC', async () => {

    await Helpers.testIdentifier(
      true, testResolver, <KeyStoreInMemory> options.keyStore, alg[0], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(options);
        return identifier.createLinkedIdentifier('did:ion-test:peerforregister', register);
      });
  });
/*
  Not yet supported in sidetree core
  it('should create an identifier and register for RSA', async done => {
    await Helpers.testIdentifier(
      true, testResolver, options.keyStore as KeyStoreInMemory, alg[1], async (options: UserAgentOptions, identifier: Identifier, register: boolean) => {
        console.log(options);
        return identifier.createLinkedIdentifier('did:ion:peer', register);
      });
    done();
  });
*/
});
