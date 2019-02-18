/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, UserServiceEndpoint, HostServiceEndpoint, ServiceReference } from '../src/types';
import IdentifierDocument from '../src/IdentifierDocument';

describe('IdentifierDocument', () => {

  it('should construct new instance when provided a document', async done => {
    const document = { 
      id: 'did:test:identifier', 
      created: '2019-01-25T01:08:44.732Z',
      publicKey: [
        {
          id: '#master',
          type: 'RsaVerificationKey2018',
          publicKeyJwk: {
            kty: 'RSA',
            kid: '#master',
            keyOps: [
              'sign',
              'verify'
            ],
            n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
            e: 'AQAB'
          }
        }
      ]
    }
    
    const identifierDocument = new IdentifierDocument(document);
    expect(identifierDocument).toBeDefined();
    expect(identifierDocument.id).toEqual('did:test:identifier');
    expect(identifierDocument.created).toEqual(new Date('2019-01-25T01:08:44.732Z'));
    expect(identifierDocument.publicKeys).toBeDefined();
    expect(identifierDocument.publicKeys.length).toEqual(1);
    done();
  });

  it('should create a new instance with expected properties', async done => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: {
          kty: 'RSA',
          kid: '#master',
          keyOps: [
            'sign',
            'verify'
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ]

    const identifierDocument = IdentifierDocument.create('did:test:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();
    expect(identifierDocument.id).toEqual('did:test:identifier');
    if (identifierDocument.created) {
      expect(Date.parse(identifierDocument.created.toISOString())).toBeCloseTo(Date.now(), 1);
    }
    expect(identifierDocument.publicKeys).toBeDefined();
    expect(identifierDocument.publicKeys.length).toEqual(1);
    done();
  });

  it('should add authentication references', async done => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: {
          kty: 'RSA',
          kid: '#master',
          keyOps: [
            'sign',
            'verify'
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ]

    const identifierDocument = IdentifierDocument.create('did:test:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const authenticationReference = {
      type: 'RsaVerificationKey2018',
      publicKeyReference: '#master'
    } as AuthenticationReference;

    identifierDocument.addAuthenticationReference(authenticationReference);
    expect(identifierDocument.authenticationReferences.length).toEqual(1);
    expect(identifierDocument.authenticationReferences[0].type).toEqual('RsaVerificationKey2018');
    expect(identifierDocument.authenticationReferences[0].publicKeyReference).toEqual('#master');
    done();
  });

  it('should add service reference for user service endpoint', async done => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: {
          kty: 'RSA',
          kid: '#master',
          keyOps: [
            'sign',
            'verify'
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ]

    const identifierDocument = IdentifierDocument.create('did:test:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const serviceReference = {
      type: 'ServiceReference',
      publicKeyReference: '#master',
      serviceEndpoint: {
        context: 'TestContext',
        type: 'UserServiceEndpoint',
        instances: ['test']
      }
    } as ServiceReference;

    identifierDocument.addServiceReference(serviceReference);
    expect(identifierDocument.serviceReferences.length).toEqual(1);
    expect(identifierDocument.serviceReferences[0].type).toEqual('ServiceReference');
    expect(identifierDocument.serviceReferences[0].publicKeyReference).toEqual('#master');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint).toBeDefined();
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.context).toEqual('TestContext');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.type).toEqual('UserServiceEndpoint');
    const userServiceEndpoint = identifierDocument.serviceReferences[0].serviceEndpoint as UserServiceEndpoint;
    expect(userServiceEndpoint.instances.length).toEqual(1);
    expect(userServiceEndpoint.instances[0]).toEqual('test');
    done();
  });

  it('should add service reference for host service endpoint', async done => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: {
          kty: 'RSA',
          kid: '#master',
          keyOps: [
            'sign',
            'verify'
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ]

    const identifierDocument = IdentifierDocument.create('did:test:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const serviceReference = {
      type: 'ServiceReference',
      publicKeyReference: '#master',
      serviceEndpoint: {
        context: 'TestContext',
        type: 'HostServiceEndpoint',
        locations: ['test']
      }
    } as ServiceReference;

    identifierDocument.addServiceReference(serviceReference);
    expect(identifierDocument.serviceReferences.length).toEqual(1);
    expect(identifierDocument.serviceReferences[0].type).toEqual('ServiceReference');
    expect(identifierDocument.serviceReferences[0].publicKeyReference).toEqual('#master');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint).toBeDefined();
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.context).toEqual('TestContext');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.type).toEqual('HostServiceEndpoint');
    const hostServiceEndpoint = identifierDocument.serviceReferences[0].serviceEndpoint as HostServiceEndpoint;
    expect(hostServiceEndpoint.locations.length).toEqual(1);
    expect(hostServiceEndpoint.locations[0]).toEqual('test');
    done();
  });
});
