/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HttpResolver from '../../src/resolvers/HttpResolver';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
const fetchMock = require('fetch-mock');

// Add a document to the cache
const DOCUMENT = {
  document: {
    '@context': 'https://w3id.org/did/v1',
    'id': 'did:ion:identifier'
  },
  resolverMetadata: {
    driverId: 'did:ion',
    driver: 'HttpDriver',
    duration: '100.0000ms'
  }
};

describe('HttpResolver', () => {
  afterEach(() => {
    fetchMock.restore();
  });

  it('should construct new instance of the HttpResolver', async done => {
    const resolver = new HttpResolver('https://resolver.org/', {
      timeoutInSeconds: 30
    });
    expect(resolver).toBeDefined();
    expect(resolver.url).toEqual('https://resolver.org/1.0/identifiers/');
    done();
  });

  it('should construct new instance of the HttpResolver appending trailing slash', async done => {
    const resolver = new HttpResolver('https://resolver.org', {
      timeoutInSeconds: 30
    });
    expect(resolver).toBeDefined();
    expect(resolver.url).toEqual('https://resolver.org/1.0/identifiers/');
    done();
  });

  it('should return a new identifier document', async done => {
    const resolver = new HttpResolver('https://resolver.org', {
      timeoutInSeconds: 30
    });
    const identifier = new Identifier('did:ion:identifier', {
      timeoutInSeconds: 5
    });

    fetchMock.get(
      'https://resolver.org/1.0/identifiers/did:ion:identifier',
      new Promise(resolve => resolve(DOCUMENT))
    );

    const idenfifierDocument: any = await resolver.resolve(identifier);
    expect(idenfifierDocument).toBeDefined();
    expect(idenfifierDocument.id).toEqual('did:ion:identifier');
    done();
  });

  it('should return a new identifier document when document is root', async done => {
    const resolver = new HttpResolver('https://resolver.org', {
      timeoutInSeconds: 30
    });
    const identifier = new Identifier('did:ion:identifier', {
      timeoutInSeconds: 5
    });

    fetchMock.get(
      'https://resolver.org/1.0/identifiers/did:ion:identifier',
      new Promise(resolve => resolve({
        '@context': 'https://w3id.org/did/v1',
        'id': 'did:ion:identifier'
      }))
    );

    const idenfifierDocument: any = await resolver.resolve(identifier);
    expect(idenfifierDocument).toBeDefined();
    expect(idenfifierDocument.id).toEqual('did:ion:identifier');
    done();
  });

  it('should throw UserAgentError when 404 returned by resolver', async done => {
    const resolver = new HttpResolver('https://resolver.org', {
      timeoutInSeconds: 30
    });
    const identifier = new Identifier('did:ion:identifier', {
      timeoutInSeconds: 5
    });

    fetchMock.get('https://resolver.org/1.0/identifiers/did:ion:identifier', 404);

    await resolver
      .resolve(identifier)
      .catch(error => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Identifier document not found for 'did:ion:identifier'`);
      })
      .finally(done);
  });

  it('should throw UserAgentError when 500 returned by resolver', async done => {
    const resolver = new HttpResolver('https://resolver.org');
    const identifier = new Identifier('did:ion:identifier', {
      timeoutInSeconds: 5
    });

    fetchMock.get('https://resolver.org/1.0/identifiers/did:ion:identifier', 500);

    await resolver
      .resolve(identifier)
      .catch(error => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Resolver at 'https://resolver.org/1.0/identifiers/' returned an error with 'Internal Server Error'`);
      })
      .finally(done);
  });

  it('should throw UserAgentError when fetch timeout threshold reached', async done => {
    const resolver = new HttpResolver('https://resolver.org', {
      timeoutInSeconds: 1
    });
    const identifier = new Identifier('did:ion:identifier', {
      timeoutInSeconds: 1
    });

    // Set the mock timeout to be greater than the fetch configuration
    // timeout to ensure that the fetch timeout works as expected.
    const delay = new Promise((res, _) => setTimeout(res, 1000 * 3));
    fetchMock.get('https://resolver.org/1.0/identifiers/did:ion:identifier', delay.then((_) => 404));

    await resolver
      .resolve(identifier)
      .catch(error => {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Fetch timed out.`);
      })
      .finally(done);
  });
});
