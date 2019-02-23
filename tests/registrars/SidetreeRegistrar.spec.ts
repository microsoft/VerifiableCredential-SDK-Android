/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import SidetreeRegistrar from '../../src/registrars/SidetreeRegistrar';
import IdentifierDocument from '../../src/IdentifierDocument';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
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
    expect(registrar.url).toEqual('https://registrar.org/register');
    done();
  });

  it('should construct new instance of the SidetreeRegistrar appending trailing slash', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', {
      timeoutInSeconds: 30
    });
    expect(registrar).toBeDefined();
    expect(registrar.url).toEqual('https://registrar.org/register');
    done();
  });

  it('should return a new identifier ', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', {
      timeoutInSeconds: 30
    });
 
    const identifier: Identifier = new Identifier('did:test:identifier');

    fetchMock.mock(
      function(url: any, opts: any) {
        expect(url).toEqual('https://registrar.org/register');
        expect(opts).toBeDefined();
        // Make sure the document has been passed
        const body: any = JSON.parse(opts.body);
        expect(body['@context']).toEqual(DOCUMENT['@context']);
        expect(body.id).toEqual(DOCUMENT.id);
        return true;
      },
      new Promise(resolve => resolve(identifier)),
      { method: 'POST'}
    );

    const identifierDocument = new IdentifierDocument(DOCUMENT);
    const idenfifier: any = await registrar.register(identifierDocument);
    expect(idenfifier).toBeDefined();
    expect(idenfifier.id).toEqual('did:test:identifier');
    done();
  });

  it('should throw UserAgentError when fetch timeout threshold reached', async done => {
    const registrar = new SidetreeRegistrar('https://registrar.org', {
      timeoutInSeconds: 1
    });
   
    // Set the mock timeout to be greater than the fetch configuration
    // timeout to ensure that the fetch timeout works as expected.
    const delay = new Promise((res, _) => setTimeout(res, 1000*3))
    fetchMock.post('https://registrar.org/register', delay.then((_)  => 404))

    await registrar
      .register(new IdentifierDocument(DOCUMENT))
      .catch(error => {
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
      .register(new IdentifierDocument(DOCUMENT))
      .catch(error => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Failed to register the identifier document.');
      })
      .finally(done);
  });
});
