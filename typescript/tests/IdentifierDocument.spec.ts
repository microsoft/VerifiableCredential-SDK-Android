/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, IdentifierDocumentPublicKey } from '../src/types';
import HostServiceEndpoint from '../src/serviceEndpoints/HostServiceEndpoint';
import UserServiceEndpoint from '../src/serviceEndpoints/UserServiceEndpoint';
import IdentifierDocument from '../src/IdentifierDocument';
import { KeyType } from '../src/crypto/keys/KeyTypeFactory';
import PublicKey, { KeyOperation } from '../src/crypto/keys/PublicKey';

describe('IdentifierDocument', () => {
  it('should construct new instance when provided a document', async () => {
    const document = {
      id: 'did:ion:identifier',
      created: '2019-01-25T01:08:44.732Z',
      publicKeys: [
        {
          id: '#master',
          type: 'RsaVerificationKey2018',
          publicKeyJwk: <PublicKey>{
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
    };

    const identifierDocument = new IdentifierDocument(document);
    expect(identifierDocument).toBeDefined();
    expect(identifierDocument.id).toEqual('did:ion:identifier');
    expect(identifierDocument.created).toEqual(new Date('2019-01-25T01:08:44.732Z'));
    expect(identifierDocument.publicKeys).toBeDefined();
    expect(identifierDocument.publicKeys.length).toEqual(1);
  });

  it('should create a new instance with expected properties', async () => {
    const publicKeys: IdentifierDocumentPublicKey[] = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: <PublicKey>{
          kty: KeyType.RSA,
          kid: '#master',
          keyOps: <KeyOperation[]>[
            KeyOperation.Sign,
            KeyOperation.Verify
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ];

    const identifierDocument = IdentifierDocument.create('did:ion:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();
    expect(identifierDocument.id).toEqual('did:ion:identifier');
    if (identifierDocument.created) {
      const created = Date.parse(identifierDocument.created.toISOString());
      const expected = Date.now();
      const isNear = (created >= (expected - 2)) && (created <= (expected + 2));
      expect(isNear).toBeTruthy();
    }
    expect(identifierDocument.publicKeys).toBeDefined();
    expect(identifierDocument.publicKeys.length).toEqual(1);
  });

  it('should add authentication references', async () => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: <PublicKey>{
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
    ];

    const identifierDocument = IdentifierDocument.create('did:ion:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const authenticationReference = <AuthenticationReference> {
      type: 'RsaVerificationKey2018',
      publicKeyReference: '#master'
    };

    identifierDocument.addAuthenticationReference(authenticationReference);
    expect(identifierDocument.authenticationReferences.length).toEqual(1);
    expect(identifierDocument.authenticationReferences[0].type).toEqual('RsaVerificationKey2018');
    expect(identifierDocument.authenticationReferences[0].publicKeyReference).toEqual('#master');
  });

  it('should add service reference for user service endpoint', async () => {
    const publicKeys = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: <PublicKey>{
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
    ];

    const identifierDocument = IdentifierDocument.create('did:ion:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const serviceReference = <ServiceReference> {
      type: 'ServiceReference',
      publicKeyReference: '#master',
      serviceEndpoint: {
        context: 'TestContext',
        type: 'UserServiceEndpoint',
        instances: ['test']
      }
    };

    identifierDocument.addServiceReference(serviceReference);
    expect(identifierDocument.serviceReferences.length).toEqual(1);
    expect(identifierDocument.serviceReferences[0].type).toEqual('ServiceReference');
    expect(identifierDocument.serviceReferences[0].publicKeyReference).toEqual('#master');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint).toBeDefined();
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.context).toEqual('TestContext');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.type).toEqual('UserServiceEndpoint');
    const userServiceEndpoint = <UserServiceEndpoint> identifierDocument.serviceReferences[0].serviceEndpoint;
    expect(userServiceEndpoint.instances.length).toEqual(1);
    expect(userServiceEndpoint.instances[0]).toEqual('test');
  });

  it('should add service reference for host service endpoint', async () => {
    const publicKeys: IdentifierDocumentPublicKey[] = [
      {
        id: '#master',
        type: 'RsaVerificationKey2018',
        publicKeyJwk: <PublicKey>{
          kty: KeyType.RSA,
          kid: '#master',
          keyOps: <KeyOperation[]>[
            KeyOperation.Sign,
            KeyOperation.Verify
          ],
          n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
          e: 'AQAB'
        }
      }
    ];

    const identifierDocument = IdentifierDocument.create('did:ion:identifier', publicKeys);
    expect(identifierDocument).toBeDefined();

    // Add authetication references
    const serviceReference = <ServiceReference> {
      type: 'ServiceReference',
      publicKeyReference: '#master',
      serviceEndpoint: {
        context: 'TestContext',
        type: 'HostServiceEndpoint',
        locations: ['test']
      }
    };

    identifierDocument.addServiceReference(serviceReference);
    expect(identifierDocument.serviceReferences.length).toEqual(1);
    expect(identifierDocument.serviceReferences[0].type).toEqual('ServiceReference');
    expect(identifierDocument.serviceReferences[0].publicKeyReference).toEqual('#master');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint).toBeDefined();
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.context).toEqual('TestContext');
    expect(identifierDocument.serviceReferences[0].serviceEndpoint.type).toEqual('HostServiceEndpoint');
    const hostServiceEndpoint = <HostServiceEndpoint> identifierDocument.serviceReferences[0].serviceEndpoint;
    expect(hostServiceEndpoint.locations.length).toEqual(1);
    expect(hostServiceEndpoint.locations[0]).toEqual('test');
  });
});
