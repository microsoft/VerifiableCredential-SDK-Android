/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { FlatJsonJws } from '@decentralized-identity/did-auth-jose';
import SidetreeRegistrar from '../../src/registrars/SidetreeRegistrar';
import IdentifierDocument from '../../src/IdentifierDocument';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
import InMemoryKeyStore from '../../src/keystores/InMemoryKeyStore';
import UserAgentOptions from '../../src/UserAgentOptions';
import CryptoOptions from '../../src/CryptoOptions';
import KeyStoreConstants from '../../src/keystores/KeyStoreConstants';
import KeyStoreMock from '../keystores/KeyStoreMock';
const fetchMock = require('fetch-mock');

// Add a document to the cache
const DOCUMENT = {
  '@context': 'https://w3id.org/did/v1',
  'id': 'did:test:identifier'
};

describe('SidetreeRegistrar', () => {
  afterEach(() => {
    fetchMock.restore();
  });

  it('should construct new instance of the SidetreeRegistrar', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org/', {
      timeoutInSeconds: 30
    });
    expect(registrar).toBeDefined();
    expect(registrar.url).toEqual('https://registrar.org/');
    done();
  });

  it('should construct new instance of the SidetreeRegistrar appending trailing slash', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', {
      timeoutInSeconds: 30
    });
    expect(registrar).toBeDefined();
    expect(registrar.url).toEqual('https://registrar.org/');
    done();
  });

  const options = {
    timeoutInSeconds: 30,
    keyStore: new KeyStoreMock(),
    cryptoOptions: new CryptoOptions()
  } as UserAgentOptions;
  (options.cryptoOptions as CryptoOptions).algorithm = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
  options.registrar = new SidetreeRegistrar('https://registrar.org', options);

  it('should return a new identifier ', async done => {
    // Setup registration environment
    await (options.keyStore as InMemoryKeyStore).save(KeyStoreConstants.masterSeed, Buffer.from('xxxxxxxxxxxxxxxxx'));

    let identifier: Identifier = new Identifier('did:test:identifier', options);

    fetchMock.mock(
      function(url: any, opts: any) {
        expect(url).toEqual('https://registrar.org/');
        expect(opts).toBeDefined();
        // Make sure the document has been passed
        const body: FlatJsonJws = JSON.parse(opts.body);
        expect(body.header).toBeDefined();
        expect(body.payload).toBeDefined();
        expect(body.signature).toBeDefined();
        return true;
      },
      new Promise(resolve => 
        resolve(identifier)),
      { method: 'POST'}
    );

    identifier = await identifier.createLinkedIdentifier('did:ion:peer', true);
    expect(identifier).toBeDefined();
    done();
  });
  /*
  it('should throw UserAgentError when fetch timeout threshold reached', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', {
      timeoutInSeconds: 1
    });

    // Set the mock timeout to be greater than the fetch configuration
    // timeout to ensure that the fetch timeout works as expected.
    const delay = new Promise((res, _) => setTimeout(res, 1000 * 3));
    fetchMock.post('https://registrar.org/register', delay.then((_) => 404));

    await registrar
      .register(new IdentifierDocument(DOCUMENT), '')
      .catch((error: any) => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Fetch timed out.`);
      })
      .finally(done);
  });

  it('should throw UserAgentError when error returned by registrar', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org');

    fetchMock.post('https://registrar.org/register', 404);

    await registrar
      .register(new IdentifierDocument(DOCUMENT), '')
      .catch((error: any) => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Failed to register the identifier document.');
      })
      .finally(done);
  });
*/

  it('should throw UserAgentError when generating an identifier and no public key specified', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    fetchMock.post('https://registrar.org/register', 404);

    await registrar
      .generateIdentifier(new IdentifierDocument(DOCUMENT))
      .catch(error => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('At least one public key must be specified in the identifier document.');
      })
      .finally(done);
  });

  it('should return generated identifier', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    const genesisDocument = Object.assign(DOCUMENT, {
      publicKey: [{
        id: '#master',
        type: 'Secp256k1VerificationKey2018',
        publicKeyHex: '02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1'
      }]
    });

    const identifier: Identifier = await registrar.generateIdentifier(new IdentifierDocument(genesisDocument));
    expect(identifier).toBeDefined();
    expect(identifier.id).toContain('did:ion');
    done();
  });

  it('should always return same identifier when provided same genesis document ', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', options);

    const genesisDocument = Object.assign(DOCUMENT, {
      publicKey: [{
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
    done();
  });
});
