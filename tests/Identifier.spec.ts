/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IdentifierDocument from '../src/IdentifierDocument';
import Identifier from '../src/Identifier';
import UserAgentOptions from '../src/UserAgentOptions';
import TestResolver from './resolvers/TestResolver';
import UserAgentError from '../src/UserAgentError';
import CryptoOptions from '../src/CryptoOptions';
import TestRegistrar from './registrars/TestRegistrar';
import { KeyType } from '../src/crypto/keys/KeyTypeFactory';
import { KeyUse } from '../src/crypto/keys/KeyUseFactory';
import KeyStoreInMemory from '../src/crypto/keyStore/KeyStoreInMemory';
import JwsToken from '../src/crypto/protocols/jws/JwsToken';

describe('Identifier', () => {

  let testResolver: TestResolver;
  let options: UserAgentOptions;

  beforeAll(() => {
    testResolver = new TestResolver();

    // Configure the agent options for the tests
    options = new UserAgentOptions();
    options.resolver = testResolver;
    options.timeoutInSeconds = 30;
  });

  it('should construct a storage identfier for the key', () => {
    let identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Encryption, KeyType.EC);
    expect(identifier).toEqual('did:ion:identifier-peer-enc-EC');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Encryption, KeyType.RSA);
    expect(identifier).toEqual('did:ion:identifier-peer-enc-RSA');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Signature, KeyType.EC);
    expect(identifier).toEqual('did:ion:identifier-peer-sig-EC');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Signature, KeyType.RSA);
    expect(identifier).toEqual('did:ion:identifier-peer-sig-RSA');
  });

  it('should construct new instance when provided an identifier string', () => {
    const identifier = new Identifier('did:ion:identifier', options);
    expect(identifier).toBeDefined();
    expect(identifier.id).toEqual('did:ion:identifier');
  });

  it('should construct new instance when provided an identifier document', () => {
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    const identifier = new Identifier(identifierDocument, options);
    expect(identifier).toBeDefined();
    expect(identifier.id).toEqual('did:ion:identifier');
  });

  it('should return document from local', () => {
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    const identifier = new Identifier(identifierDocument, options);

    const resolver = spyOn<any>(testResolver, 'resolve').and.callThrough();
    const localDocument = identifier.getDocument();
    expect(localDocument).toBeDefined();
    expect(resolver).not.toHaveBeenCalled();
  });

  it('should throw when no resolver specified and document not cached', async done => {
    const options: UserAgentOptions = <UserAgentOptions> {
      timeoutInSeconds: 30
    };

    const identifier = new Identifier('did:ion:identifier', options);
    await identifier
      .getDocument()
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Resolver not specified in user agent options.');
      })
      .finally(done);
  });

  it('should resolve identifier and return document', () => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);

    const resolver = spyOn(testResolver, 'resolve').and.callThrough();
    const result = identifier.getDocument();
    expect(result).toBeDefined();
    expect(resolver).toHaveBeenCalled();
  });

  it('should call getDocument() when local document undefined', async () => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
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
        ]
      }
    );

    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);

    const resolver = spyOn(testResolver, 'resolve').and.callThrough();
    const result = await identifier.getPublicKey('#master');
    expect(result).toBeDefined();
    expect(resolver).toHaveBeenCalled();
  });

  it('should throw when document has no keys', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z'
      }
    );

    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);
    await identifier
      .getPublicKey('#master')
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Document does not contain any public keys');
      })
      .finally(done);
  });

  it('should throw when document does not contain specified key', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
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
        ]
      }
    );

    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);
    await identifier
      .getPublicKey('#notInDocument')
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Document does not contain a key with id '#notInDocument'`);
      })
      .finally(done);
  });

  it('should return public key for specified key identifier', async () => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
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
        ]
      }
    );

    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);
    const publicKey = await identifier.getPublicKey('#master');
    expect(publicKey).toBeDefined();
    expect(publicKey.id).toEqual('#master');
  });

  it('should return first public key when no key identifier specified', async () => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z',
        publicKeys: [
          {
            id: '#first',
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
          },
          {
            id: '#second',
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
    );

    (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);
    const publicKey = await identifier.getPublicKey();
    expect(publicKey).toBeDefined();
    expect(publicKey.id).toEqual('#first');
  });

  describe('sign', () => {

    let options: UserAgentOptions;

    beforeEach(() => {
      options = new UserAgentOptions();
      options.resolver = testResolver;
      options.timeoutInSeconds = 30;
      options.didPrefix = 'did:ion';
      options.registrar = new TestRegistrar();
    });

    it('should sign a payload that is a string', async () => {
      options.cryptoOptions = new CryptoOptions();
      options.cryptoOptions.authenticationSigningJoseAlgorithm = 'ES256K';
      options.keyStore = new KeyStoreInMemory();
      await options.keyStore.save('masterSeed', Buffer.from('xxxxxxxxxxxxxxxxx'));
      const identifier = await Identifier.create(options);
      const signedPayload = await identifier.sign('examplePayload', 'did:ion-did:ion-EC-sig');
      expect(signedPayload).toBeDefined();
      expect(JwsToken.deserialize(signedPayload).getPayload()).toEqual('examplePayload');
    });

    it('should sign a payload that is an object', async () => {
      options.cryptoOptions = new CryptoOptions();
      options.cryptoOptions.authenticationSigningJoseAlgorithm = 'ES256K';
      options.keyStore = new KeyStoreInMemory();
      await options.keyStore.save('masterSeed', Buffer.from('xxxxxxxxxxxxxxxxx'));
      const identifier = await Identifier.create(options);
      const signedPayload = await identifier.sign({ payload: 'examplePayload' }, 'did:ion-did:ion-EC-sig');
      expect(signedPayload).toBeDefined();
      expect(JwsToken.deserialize(signedPayload).getPayload()).toEqual(`{"payload":"examplePayload"}`);
    });
  });

  describe('verify', () => {
    let identifier: Identifier;
    let identifierDocument: IdentifierDocument;

    beforeAll(() => {
      identifier = new Identifier('did:ion:identifier', options);

      identifierDocument = new IdentifierDocument(
        {
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
          ]
        }
      );
      console.log(identifier);
      console.log(identifierDocument);
    });

    /*
    const testJws = 'testJWS';
    fit('should resolve identifier document and verify jws', async () => {
      (<TestResolver> options.resolver).prepareTest(identifier, identifierDocument);
      const verifiedPayload = await identifier.verify(testJws);
      expect(verifiedPayload).toBeDefined();
      expect(verifiedPayload).toBe('verifiedPayload');
    });

    fit('should verify jws', async () => {
      console.log(identifier);
      const testIdentifier = new Identifier(identifierDocument, options);
      const verifiedPayload = await testIdentifier.verify(testJws);
      expect(verifiedPayload).toBeDefined();
      expect(verifiedPayload).toBe('verifiedPayload');
    });
  */
  });
});
