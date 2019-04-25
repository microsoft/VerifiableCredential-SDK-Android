import Protect from '../../src/keystores/Protect';
import { DidKey, KeyExport } from '@decentralized-identity/did-crypto-typescript';
import CryptoOptions from '../../src/CryptoOptions';
import KeyStoreMock from './KeyStoreMock';
import { SignatureFormat } from '../../src/keystores/SignatureFormat';
import { PublicKey } from '../../src/types';
import UserAgentError from '../../src/UserAgentError';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

describe('Protect', () => {
  describe('verify', () => {

    const payload = 'example payload';

    it('should verify a jws using RSA keys', async done => {
      const didKey = new DidKey(
        (new CryptoOptions()).cryptoApi,
        { name: 'RSASSA-PKCS1-v1_5', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]), hash: { name: 'SHA-256' } },
        null
      );
      const privateKey = await didKey.getJwkKey(KeyExport.Private);
      const jwk: any = await didKey.getJwkKey(KeyExport.Public);
      const publicKey: PublicKey = {
        id: jwk.kid,
        type: 'RsaVerificationKey2018',  // TODO switch by leveraging pairwiseKey
        publicKeyJwk: jwk
      };
      const keyStore = new KeyStoreMock();
      await keyStore.save('key', privateKey);
      const signature = await keyStore.sign('key', payload, SignatureFormat.FlatJsonJws);
      const result = await Protect.verify(signature, [publicKey]);
      expect(result).toBeDefined();
      expect(result).toBe(payload);
      done();
    });

    it('should verify a jws using EC keys', async done => {
      const didKey = new DidKey(
        (new CryptoOptions()).cryptoApi,
        { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
        null
      );
      const privateKey = await didKey.getJwkKey(KeyExport.Private);
      const jwk: any = await didKey.getJwkKey(KeyExport.Public);
      const publicKey: PublicKey = {
        id: jwk.kid,
        type: 'Secp256k1VerificationKey2018',
        publicKeyJwk: jwk
      };
      const keyStore = new KeyStoreMock();
      await keyStore.save('key', privateKey);
      const signature = await keyStore.sign('key', payload, SignatureFormat.FlatJsonJws);
      const result = await Protect.verify(signature, [publicKey]);
      expect(result).toBeDefined();
      expect(result).toBe(payload);
      done();
    });

    it('should throw an error because key type is not supported', async done => {
      const didKey = new DidKey(
        (new CryptoOptions()).cryptoApi,
        { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
        null
      );
      const privateKey = await didKey.getJwkKey(KeyExport.Private);
      const jwk: any = await didKey.getJwkKey(KeyExport.Public);
      const publicKey: PublicKey = {
        id: jwk.kid,
        type: 'Secp256k1VerificationKey2018',
        publicKeyJwk: jwk
      };
      const keyStore = new KeyStoreMock();
      await keyStore.save('key', privateKey);
      const signature = await keyStore.sign('key', payload, SignatureFormat.FlatJsonJws);
      publicKey.publicKeyJwk.kty = 'AA';
      try {
        await Protect.verify(signature, [publicKey]);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('The key type \'AA\' is not supported.');
      }
      done();
    });

    it('should throw an error because no public key matches the header key id', async done => {
      const didKey = new DidKey(
        (new CryptoOptions()).cryptoApi,
        { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
        null
      );
      const privateKey = await didKey.getJwkKey(KeyExport.Private);
      const jwk: any = await didKey.getJwkKey(KeyExport.Public);
      const publicKey: PublicKey = {
        id: 'wrongKey',
        type: 'Secp256k1VerificationKey2018',
        publicKeyJwk: jwk
      };
      const keyStore = new KeyStoreMock();
      await keyStore.save('key', privateKey);
      const signature = await keyStore.sign('key', payload, SignatureFormat.FlatJsonJws);
      try {
        await Protect.verify(signature, [publicKey]);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('No Public Key \'#key1\'');
      }
      done();
    });

    it('should throw an error because signature could not be verified', async done => {
      const didKey = new DidKey(
        (new CryptoOptions()).cryptoApi,
        { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
        null
      );
      const jwk: any = await didKey.getJwkKey(KeyExport.Public);
      const publicKey: PublicKey = {
        id: jwk.id,
        type: 'Secp256k1VerificationKey2018',
        publicKeyJwk: jwk
      };
      try {
        await Protect.verify('wrongSignature', [publicKey]);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('JWS Token cannot be verified by public key: \'Error: Could not parse contents into a JWS\'');
      }
      done();
    });
  });
});
