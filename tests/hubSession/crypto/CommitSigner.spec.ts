/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { ICommitProtectedHeaders } from '@decentralized-identity/hub-common-js';
import CommitSigner from '../../../src/hubSession/crypto/CommitSigner';
import { EcPrivateKey, Secp256k1CryptoSuite } from '@decentralized-identity/did-auth-jose';
import Commit from '../../../src/hubSession/Commit';
import RsaPrivateKey from '../../../src/crypto/keys/rsa/RsaPrivateKey';
import KeyStoreInMemory from '../../../src/crypto/keyStore/KeyStoreInMemory';
import SubtleCryptoOperations from '../../../src/crypto/plugin/SubtleCryptoOperations';
import { SubtleCrypto } from 'webcrypto-core';
import PrivateKey from '../../../src/crypto/keys/PrivateKey';

describe('CommitSigner', () => {

  describe('sign()', () => {

    fit('should sign a commit using Rsa', async () => {
      const testDid = 'did:example:person.id';
      const testKid = `${testDid}#key-1`;
      const crypto = new SubtleCrypto();
      const key = await crypto.generateKey('RSA', false, ['sign']);
      console.log(key);
      const testKeyStore = new KeyStoreInMemory();
      // testKeyStore.save(testKid, key);

      const protectedHeaders: Partial<ICommitProtectedHeaders> = {
        interface: 'Collections',
        context: 'schema.org',
        type: 'MusicPlaylist',
        operation: 'create',
        committed_at: '2019-01-01',
        commit_strategy: 'basic',
        sub: 'did:example:sub.id',
        // iss and kid left out intentionally
      };

      const payload = {
        name: "Test"
      };

      const commit = new Commit({
        protected: protectedHeaders,
        payload
      });

      const signer = new CommitSigner({
        did: testDid,
        keyReference: testKid,
        keyStore: testKeyStore
      });

      const signedCommit = await signer.sign(commit);

      expect(signedCommit.getPayload()).toEqual(payload);

      const signedProtectedHeaders = signedCommit.getProtectedHeaders();
      Object.keys(protectedHeaders).forEach((headerKey) => {
        expect((<any> signedProtectedHeaders)[headerKey]).toEqual((<any> protectedHeaders)[headerKey]);
      })

      expect(signedProtectedHeaders.iss).toEqual(testDid);
      expect(signedProtectedHeaders.kid).toEqual(testKid);
    });

    // it('should sign a commit using EC', async () => {
    //   const testDid = 'did:example:person.id';
    //   const testKid = `${testDid}#key-1`;
    //   const testKey = await EcPrivateKey.generatePrivateKey(testKid);

    //   const protectedHeaders: Partial<ICommitProtectedHeaders> = {
    //     interface: 'Collections',
    //     context: 'schema.org',
    //     type: 'MusicPlaylist',
    //     operation: 'create',
    //     committed_at: '2019-01-01',
    //     commit_strategy: 'basic',
    //     sub: 'did:example:sub.id',
    //     // iss and kid left out intentionally
    //   };

    //   const payload = {
    //     name: "Test"
    //   };

    //   const commit = new Commit({
    //     protected: protectedHeaders,
    //     payload
    //   });

    //   const signer = new CommitSigner({
    //     did: testDid,
    //     key: testKey,
    //     cryptoSuite: new Secp256k1CryptoSuite()
    //   });

    //   const signedCommit = await signer.sign(commit);

    //   expect(signedCommit.getPayload()).toEqual(payload);

    //   const signedProtectedHeaders = signedCommit.getProtectedHeaders();
    //   Object.keys(protectedHeaders).forEach((headerKey) => {
    //     expect((<any> signedProtectedHeaders)[headerKey]).toEqual((<any> protectedHeaders)[headerKey]);
    //   })

    //   expect(signedProtectedHeaders.iss).toEqual(testDid);
    //   expect(signedProtectedHeaders.kid).toEqual(testKid);
    // });

    // it('should throw an error if a commit is not valid', async () => {
    //   const testDid = 'did:example:person.id';
    //   const testKid = `${testDid}#key-1`;
    //   const testKey = await RsaPrivateKey.generatePrivateKey(testKid);

    //   const commit = new Commit({
    //     protected: {
    //       interface: 'Collections',
    //       context: 'schema.org',
    //       // type: 'MusicPlaylist', // left out intentionally
    //       operation: 'create',
    //       committed_at: '2019-01-01',
    //       commit_strategy: 'basic',
    //       sub: 'did:example:sub.id',
    //     },
    //     payload: {
    //       name: "Test"
    //     }
    //   });

    //   const signer = new CommitSigner({
    //     did: testDid,
    //     key: testKey
    //   });

    //   try {
    //     await signer.sign(commit);
    //     fail('Not expected to reach this point.');
    //   } catch (err) {
    //     expect(err.message).toContain("Commit 'protected.type' field must be");
    //   }
    // });

  });
});
