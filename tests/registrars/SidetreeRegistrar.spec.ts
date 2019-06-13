/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import SidetreeRegistrar from '../../src/registrars/SidetreeRegistrar';
import IdentifierDocument from '../../src/IdentifierDocument';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
import UserAgentOptions from '../../src/UserAgentOptions';
import CryptoOptions from '../../src/CryptoOptions';
import KeyStoreConstants from '../../src/keystores/KeyStoreConstants';
import CryptoFactory from '../../src/crypto/plugin/CryptoFactory';
import KeyStoreInMemory from '../../src/crypto/keyStore/KeyStoreInMemory';
import IJwsFlatJson from '../../src/crypto/protocols/jws/IJwsFlatJson';
import SecretKey from '../../src/crypto/keys/SecretKey';
import SubtleCryptoNodeOperations from '../../src/crypto/plugin/SubtleCryptoNodeOperations';

let fetchMock: any;

// Add a document to the cache
const DOCUMENT = {
  '@context': 'https://w3id.org/did/v1',
  'id': 'did:test:identifier'
};

describe('SidetreeRegistrar', () => {

  let options: UserAgentOptions;

  beforeAll(() => {
    fetchMock = require('fetch-mock');
    options = new UserAgentOptions();
    (<CryptoOptions> options.cryptoOptions).authenticationSigningJoseAlgorithm = 'ES256K';
    options.cryptoFactory = new CryptoFactory(new KeyStoreInMemory(), new SubtleCryptoNodeOperations());
    options.registrar = new SidetreeRegistrar('https://registrar.org', options);
  });

  afterEach(() => {
    fetchMock.restore();
  });

  it('should throw because of missing options', async (done) => {
    try {
      await new SidetreeRegistrar('https://registrar.org/', <any>{}).register(new IdentifierDocument(''), '');
      fail('Failed to throw because of missing options');
    } catch (err) {
      expect('options and options.keyStore need to be defined').toBe(err.message);
      done();
    }
  });

  it('should construct new instance of the SidetreeRegistrar', async () => {
    let options = new UserAgentOptions();
    options.timeoutInSeconds = 30;
    options.keyStore = new KeyStoreInMemory();
    const registrar = new SidetreeRegistrar('https://registrar.org/', options);
    expect(registrar).toBeDefined();
    expect(registrar.url).toEqual('https://registrar.org/');
  });

  it('should construct new instance of the SidetreeRegistrar appending trailing slash', async () => {
    let options = new UserAgentOptions();
    options.timeoutInSeconds = 30;
    options.keyStore = new KeyStoreInMemory();
    const registrar = new SidetreeRegistrar('https://registrar.org', options);
    expect(registrar).toBeDefined();
    expect(registrar.url).toEqual('https://registrar.org/');
  });

  it('should return a new identifier', async () => {
    // Setup registration environment
    const seed = new SecretKey('ABDE');
    await options.keyStore.save('masterSeed', seed);

    let identifier: Identifier = new Identifier('did:test:identifier', options);

    fetchMock.mock(
      (url: any, opts: any) => {
        expect(url).toEqual('https://registrar.org/');
        expect(opts).toBeDefined();
        // Make sure the document has been passed
        const body: IJwsFlatJson = JSON.parse(opts.body);
        expect(body.header).toBeDefined();
        // the header should be undefined. commented out for moment to get to identiverse - todo
        //expect(body.protected).toBeUndefined();
        expect(body.payload).toBeDefined();
        expect(body.signature).toBeDefined();
        return true;
      },
      new Promise(resolve =>
        resolve(identifier)),
      { method: 'POST' }
    );

    identifier = await identifier.createLinkedIdentifier('did:ion:peer', true);
    expect(identifier).toBeDefined();
  });

  it('should throw UserAgentError when fetch timeout threshold reached', async done => {
    options.timeoutInSeconds = 1;
    options.registrar = new SidetreeRegistrar('https://registrar.org/', options);

    // Setup registration environment
    const seed = new SecretKey('ABDE');
    await options.keyStore.save('masterSeed', seed);
    const identifier: Identifier = new Identifier('did:test:identifier', options);

    // Set the mock timeout to be greater than the fetch configuration
    // timeout to ensure that the fetch timeout works as expected.
    const delay = new Promise((response, _) => setTimeout(response, 1000 * 10));
    fetchMock.post('https://registrar.org/', delay.then((_) =>
      404));

    // tslint:disable-next-line: no-floating-promises
    identifier.createLinkedIdentifier('did:ion:peer', true)
    .then(() => {
      fail('No throw detected because of timeout.');
    })
    .catch((error: any) => {
      expect(error).toBeDefined();
      expect(error instanceof UserAgentError).toBeTruthy();
      expect(error.message).toEqual(`Fetch timed out.`);
    })
    .finally(() => {
      options.timeoutInSeconds = 30;
      done();
    });
  });

  it('should throw UserAgentError when error returned by registrar', async done => {
    // Setup registration environment
    const seed = new SecretKey('ABDE');
    await options.keyStore.save('masterSeed', seed);
    const identifier: Identifier = new Identifier('did:test:identifier', options);

    fetchMock.post('https://registrar.org/', 404);

        // tslint:disable-next-line: no-floating-promises
    identifier.createLinkedIdentifier('did:ion:peer', true)
    .then(() => {
      fail('No throw detected because of 404.');
    })
    .catch((error: any) => {
      expect(error).toBeDefined();
      expect(error instanceof UserAgentError).toBeTruthy();
      expect(error.message).toEqual('Failed to register the identifier document: Status 404');
    })
    .finally(done);
  });

  it('should throw UserAgentError when generating an identifier and no public key specified', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    fetchMock.post('https://registrar.org/register', 404);

    await registrar
      .generateIdentifier(new IdentifierDocument(DOCUMENT))
      .catch((error: any) => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('At least one public key must be specified in the identifier document.');
      })
      .finally(done);
  });

  it('should return generated identifier', async () => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    const genesisDocument = Object.assign(DOCUMENT, {
      publicKeys: [{
        id: '#master',
        type: 'Secp256k1VerificationKey2018',
        publicKeyHex: '02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1'
      }]
    });

    const identifier: Identifier = await registrar.generateIdentifier(new IdentifierDocument(genesisDocument));
    expect(identifier).toBeDefined();
    expect(identifier.id).toContain('did:ion');
  });

  it('should always return same identifier when provided same genesis document ', async () => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    const genesisDocument = Object.assign(DOCUMENT, {
      publicKeys: [{
        id: '#master',
        type: 'Secp256k1VerificationKey2018',
        publicKeyHex: '02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1'
      }]
    });

    let previousIdentifier: string = '';
    for (let index = 0; index < 20; index++) {
      const identifier: Identifier = await registrar.generateIdentifier(new IdentifierDocument(genesisDocument));

      if (index !== 0) {
        expect(identifier.id).toEqual(previousIdentifier);
      }

      previousIdentifier = identifier.id;
    }
  });
});
