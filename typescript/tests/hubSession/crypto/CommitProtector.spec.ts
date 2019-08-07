/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CommitProtector, { CommitProtectorOptions } from '../../../src/hubSession/crypto/CommitProtector';
import { PrivateKey, KeyStoreInMemory, SecretKey, SubtleCryptoNodeOperations, SubtleCryptoExtension, Commit, HubInterfaceType, CommitStrategyType } from '../../../src';
import { IJwsSigningOptions } from '../../../src/crypto/protocols/jose/IJoseOptions';
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import ProtectionStrategy from '../../../src/crypto/strategies/ProtectionStrategy';
import PayloadSigningStrategy from '../../../src/crypto/strategies/PayloadSigningStrategy';
import JoseProtocol from '../../../src/crypto/protocols/jose/JoseProtocol';
import { Operation } from '../../../src/hubInterfaces/HubInterface';
import { ICommitFields } from '../../../src/hubSession/Commit';
import JoseConstants from '../../../src/crypto/protocols/jose/JoseConstants';
import IPayloadProtectionOptions from '../../../src/crypto/protocols/IPayloadProtectionOptions';
import { TSMap } from 'typescript-map';

describe('CommitProtector', () => {
  const did = 'did:ion:test';
  const payloadProtection = new JoseProtocol();
  const payloadData = 'payload';
  const signingKeyReference = 'signingKey';
  const encryptionKeyReference = 'encryptionKey';
  
  const commitFields: ICommitFields = {
    interface: HubInterfaceType.Collections,
    context: 'schema.org',
    type: 'MusicPlaylist',
    operation: Operation.Create,
    committed_at: '2019-01-01',
    commit_strategy: CommitStrategyType.Basic,
    iss: 'did:example:sub.id',
    sub: 'did:example:sub.id',
    payload: payloadData,
    object_id: undefined
  };
  const keyStore = new KeyStoreInMemory();
  let signingKey: PrivateKey; 
  let cryptoFactory: CryptoFactory;

  beforeAll(async () => {
    const seedReference = 'seed';
    await keyStore.save(seedReference, new SecretKey('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'));
    const cryptoSuite = new SubtleCryptoNodeOperations();
    cryptoFactory = new CryptoFactory(keyStore, cryptoSuite);
    let alg: any = <Algorithm> { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } };
    const generate = new SubtleCryptoExtension(cryptoFactory);

    // Generate signing key
    signingKey = await generate.generatePairwiseKey(alg, seedReference, 'did:personaId', 'did:peerId');
    (<any>signingKey).alg = 'ES256K';
    (<any>signingKey).defaultSignAlgorithm = 'ES256K';
      
    await keyStore.save(signingKeyReference, signingKey);

    // Generate encryption key
    alg = { name: 'RSA-OAEP', hash: 'SHA-256', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]) };
    const encryptionKey = await generate.generatePairwiseKey(alg, seedReference, 'did:personaId', 'did:peerId');
    await keyStore.save(encryptionKeyReference, encryptionKey);
  });

  it('should sign and encrypt the commit', async () => {
    const options: IPayloadProtectionOptions = {
      cryptoFactory: cryptoFactory,
      options: new TSMap<string, any>(),
      payloadProtection: new JoseProtocol()
  };

    const commitProtectOptions: CommitProtectorOptions = {
      did: did, 
      signingKeyReference: signingKeyReference,
      recipientsPublicKeys: [await keyStore.get(encryptionKeyReference, true)],
      payloadProtection: payloadProtection,
      payloadProtectionOptions: options,
      hubProtectionStrategy: undefined,
    };

    commitFields.payload = {data: 'data'};
    const commit = new Commit(commitFields);
     
    const commitProtector = new CommitProtector(commitProtectOptions);

    const protectedCommit = await commitProtector.protect(commit);
    expect(protectedCommit.get(JoseConstants.tokenCiphertext)).toBeDefined();
    expect(protectedCommit.get(JoseConstants.tokenSignatures)).toBeUndefined();
    commitFields.payload = payloadData;
  });

  it('should throw with bad optiona', async () => {
    const protectionStrategy = new ProtectionStrategy();

    // Missing signing key
    const options: IPayloadProtectionOptions = {
      cryptoFactory: cryptoFactory,
      options: new TSMap<string, any>(),
      payloadProtection: new JoseProtocol()
  };
    let commitProtectOptions: CommitProtectorOptions = {
      did: did, 
      signingKeyReference: undefined,
      recipientsPublicKeys: undefined,
      payloadProtection: payloadProtection,
      payloadProtectionOptions: options,
      hubProtectionStrategy: protectionStrategy
    };

    let throwed = false;
    let commitProtector = new CommitProtector(commitProtectOptions);
    let commit = new Commit(commitFields);
    try { 
      await commitProtector.protect(commit);
    } catch(err) {
      throwed = true;
      expect(err.message).toEqual('The signing key reference is missing from the options');
    } 

    expect(throwed).toBeTruthy();

    // Missing encryption key
    throwed = false;
    protectionStrategy.payloadSigningStrategy.enabled = false;
    commitProtector = new CommitProtector(commitProtectOptions);
    commit = new Commit(commitFields);
    try { 
      await commitProtector.protect(commit);
    } catch(err) {
      throwed = true;
      expect(err.message).toEqual('The encryption public keys are missing from the options');
    } 

    expect(throwed).toBeTruthy();
  });

  it('should sign the commit', async () => {
    const protectionStrategy = new ProtectionStrategy();
    protectionStrategy.PayloadEncryptionStrategy.enabled = false;
    const options: IPayloadProtectionOptions = {
      cryptoFactory: cryptoFactory,
      options: new TSMap<string, any>(),
      payloadProtection: new JoseProtocol()
    };

    const commitProtectOptions: CommitProtectorOptions = {
      did: did, 
      signingKeyReference: signingKeyReference,
      recipientsPublicKeys: undefined,
      payloadProtection: payloadProtection,
      payloadProtectionOptions: options,
      hubProtectionStrategy: protectionStrategy
    };

    const commit = new Commit(commitFields);
     
    const commitProtector = new CommitProtector(commitProtectOptions);

    const protectedCommit = await commitProtector.protect(commit);
    expect(protectedCommit.get(JoseConstants.tokenPayload)).toEqual(Buffer.from(payloadData));
    expect(protectedCommit.get(JoseConstants.tokenSignatures)).toBeDefined();
  });

  it('should encrypt the commit', async () => {
    const protectionStrategy = new ProtectionStrategy();
    protectionStrategy.payloadSigningStrategy.enabled = false;

    const options: IPayloadProtectionOptions = {
      cryptoFactory: cryptoFactory,
      options: new TSMap<string, any>(),
      payloadProtection: new JoseProtocol()
    };
    const commitProtectOptions: CommitProtectorOptions = {
      did: did, 
      signingKeyReference: undefined,
      recipientsPublicKeys: [await keyStore.get(encryptionKeyReference, true)],
      payloadProtection: payloadProtection,
      payloadProtectionOptions: options,
      hubProtectionStrategy: protectionStrategy,
    };

    commitFields.payload = {data: 'data'};
    const commit = new Commit(commitFields);
     
    const commitProtector = new CommitProtector(commitProtectOptions);

    const protectedCommit = await commitProtector.protect(commit);
    expect(protectedCommit.get(JoseConstants.tokenCiphertext)).toBeDefined();
    expect(protectedCommit.get(JoseConstants.tokenSignatures)).toBeUndefined();
  });
});
