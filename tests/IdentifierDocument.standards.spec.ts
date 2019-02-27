/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IdentifierDocument from '../src/IdentifierDocument';

describe('IdentifierDocument [Standards Compliance]', () => {

  it('should serialize expected JSON-LD excluding empty properties', async done => {
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
      ],
      authentication:[]
    }
    
    const identifierDocument = new IdentifierDocument(document);
    const serializedDocument = JSON.stringify(identifierDocument);
    expect(serializedDocument).toEqual('{"publicKeys":[{"id":"#master","type":"RsaVerificationKey2018","publicKeyJwk":{"kty":"RSA","kid":"#master","keyOps":["sign","verify"],"n":"vdpHn7kNq42UMC1W8bwxgE7K...","e":"AQAB"}}],"id":"did:test:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1"}');
    done();
  });

  it('should serialize expected JSON-LD', async done => {
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
      ],
      authentication:[{ type: 'RsaVerificationKey2018', publicKeyReference: '#master'}],
      service:[]
    }
    
    const identifierDocument = new IdentifierDocument(document);
    const serializedDocument = JSON.stringify(identifierDocument);
    expect(serializedDocument).toEqual('{"publicKeys":[{"id":"#master","type":"RsaVerificationKey2018","publicKeyJwk":{"kty":"RSA","kid":"#master","keyOps":["sign","verify"],"n":"vdpHn7kNq42UMC1W8bwxgE7K...","e":"AQAB"}}],"authenticationReferences":[{"type":"RsaVerificationKey2018","publicKeyReference":"#master"}],"id":"did:test:identifier","created":"2019-01-25T01:08:44.732Z","@context":"https://w3id.org/did/v1"}');
    done();
  });
});
