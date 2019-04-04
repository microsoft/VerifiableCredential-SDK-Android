/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IdentifierDocument from '../src/IdentifierDocument';

describe('IdentifierDocument [Standards Compliance]', () => {

  it('should serialize expected JSON-LD excluding empty properties', async done => {
    const document = {
      id: 'did:ion:identifier',
      created: '2019-01-25T01:08:44.732Z',
      publicKeys: [
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
      ],
      authentication: []
    };

    const identifierDocument = new IdentifierDocument(document);
    const serializedDocument = JSON.stringify(identifierDocument);
    /* tslint:disable:max-line-length */
    expect(serializedDocument).toEqual('{"id":"did:ion:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1","publicKey":[{"id":"#master","type":"RsaVerificationKey2018","publicKeyJwk":{"kty":"RSA","kid":"#master","keyOps":["sign","verify"],"n":"vdpHn7kNq42UMC1W8bwxgE7K...","e":"AQAB"}}]}');
    done();
  });

  it('should serialize expected JSON-LD', async done => {
    const document = {
      id: 'did:ion:identifier',
      created: '2019-01-25T01:08:44.732Z',
      publicKeys: [
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
      ],
      authentication: [{ type: 'RsaVerificationKey2018', publicKeyReference: '#master' }],
      service: []
    };

    const identifierDocument = new IdentifierDocument(document);
    const serializedDocument = JSON.stringify(identifierDocument);
    expect(serializedDocument).toEqual('{"authenticationReferences":[{"type":"RsaVerificationKey2018","publicKeyReference":"#master"}],"id":"did:ion:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1","publicKey":[{"id":"#master","type":"RsaVerificationKey2018","publicKeyJwk":{"kty":"RSA","kid":"#master","keyOps":["sign","verify"],"n":"vdpHn7kNq42UMC1W8bwxgE7K...","e":"AQAB"}}]}');
    delete identifierDocument.publicKeys;
    expect(JSON.stringify(identifierDocument)).toEqual('{"authenticationReferences":[{"type":"RsaVerificationKey2018","publicKeyReference":"#master"}],"id":"did:ion:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1"}');
    done();
  });

  it('should deserialize JSON-LD', async done => {
    const document = {
      id: 'did:ion:identifier',
      created: '2019-01-25T01:08:44.732Z',
      publicKeys: [
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
      ],
      authenticationReferences: [{ type: 'RsaVerificationKey2018', publicKeyReference: '#master' }]
    };

    const serializedDocument = '{"authenticationReferences":[{"type":"RsaVerificationKey2018","publicKeyReference":"#master"}],"id":"did:ion:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1","publicKey":[{"id":"#master","type":"RsaVerificationKey2018","publicKeyJwk":{"kty":"RSA","kid":"#master","keyOps":["sign","verify"],"n":"vdpHn7kNq42UMC1W8bwxgE7K...","e":"AQAB"}}]}';
    const deserializedDocument = IdentifierDocument.fromJSON(JSON.parse(serializedDocument));
    expect(document.id).toBe(deserializedDocument.id);
    expect(1).toBe(deserializedDocument.publicKeys.length);
    expect(document.authenticationReferences).toEqual(deserializedDocument.authenticationReferences);
    done();
  });
});
