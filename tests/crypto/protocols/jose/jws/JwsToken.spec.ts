/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from "base64url";
import JwsToken from "../../../../../src/crypto/protocols/jose/jws/JwsToken";
import SecretKey from "../../../../../src/crypto/keys/SecretKey";
import KeyStoreInMemory from "../../../../../src/crypto/keyStore/KeyStoreInMemory";
import CryptoFactory from "../../../../../src/crypto/plugin/CryptoFactory";
import SubtleCryptoNodeOperations from "../../../../../src/crypto/plugin/SubtleCryptoNodeOperations";
import { ProtectionFormat } from "../../../../../src/crypto/keyStore/ProtectionFormat";
import { SubtleCryptoExtension } from "../../../../../src";
import { SubtleCryptoElliptic } from '@microsoft/useragent-plugin-secp256k1';
import { IJwsSigningOptions } from "../../../../../src/crypto/protocols/jose/IJoseOptions";
import IPayloadProtectionProtocolOptions from "../../../../../src/crypto/protocols/IPayloadProtectionProtocolOptions";
import JoseProtocol from "../../../../../src/crypto/protocols/jose/JoseProtocol";
import { TSMap } from "typescript-map";
import JoseConstants from "../../../../../src/crypto/protocols/jose/JoseConstants";

describe('JwsToken', () => {
  it('should create a jws token', async () => {
    const payload = 'test payload';
    const keyStore = new KeyStoreInMemory();
    const seedReference = 'seed';
    await keyStore.save(seedReference, new SecretKey('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'));
    const cryptoSuite = new SubtleCryptoNodeOperations();
    const options: IJwsSigningOptions = {
        algorithm: <Algorithm> { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } },
        cryptoFactory: new CryptoFactory(keyStore, cryptoSuite)
      };
      const generate = new SubtleCryptoExtension(options.cryptoFactory);

      const privateKey = await generate.generatePairwiseKey(options.algorithm, seedReference, 'did:personaId', 'did:peerId');
      (<any>privateKey).alg = 'ES256K';
      (<any>privateKey).defaultSignAlgorithm = 'ES256K';
      
      await keyStore.save('key', privateKey);
      const jwsToken = new JwsToken(options);
      const signature = await jwsToken.sign('key', Buffer.from(payload), ProtectionFormat.JwsGeneralJson);
      expect(signature).toBeDefined();
  });
  it('should create, validate and serialize a JwsToken', async () => {
    const payload = 'The true sign of intelligence is not knowledge but imagination.';      
    const keyStore = new KeyStoreInMemory();
    await keyStore.save('seed', new SecretKey('ABEE'));
    const cryptoFactory = new CryptoFactory(keyStore, new SubtleCryptoNodeOperations())
    const options: IPayloadProtectionProtocolOptions = {
        cryptoFactory: cryptoFactory,
        protocolOption: new TSMap<string, any>(),
        protocolInterface: new JoseProtocol()
    };
 
    const alg = { name: 'RSASSA-PKCS1-V1_5', hash: 'SHA-256', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]) };
    const generator = new SubtleCryptoExtension(cryptoFactory);
    const privateKey = await generator.generatePairwiseKey(alg, 'seed', 'persona','peer');
    expect((<any>privateKey).alg).toBeUndefined();
    (<any>privateKey).alg = 'RS256';
    await keyStore.save('key', privateKey);

    // sign
    const signature = await options.protocolInterface.sign('key', Buffer.from(payload), 'JwsGeneralJson', options);
    const signatures = signature.get(JoseConstants.tokenSignatures);
    expect(signatures[0].protected).toBeDefined();
    expect(signatures[0].signature).toBeDefined();
    expect(signature.get(JoseConstants.tokenPayload)).toEqual(Buffer.from(payload));

    // serialize
    let serialized = options.protocolInterface.serialize(signature, 'JwsGeneralJson', options);
    let parsed = JSON.parse(serialized);
    expect(parsed['payload']).toBeDefined();
    expect(parsed['signatures']).toBeDefined();

    // deserialize
    let deserialized = options.protocolInterface.deserialize(serialized, 'JwsGeneralJson', options);
    let deSignatures = deserialized.get(JoseConstants.tokenSignatures);
    expect(deSignatures[0].protected).toEqual(signatures[0].protected);
    expect(deSignatures[0].signature).toEqual(signatures[0].signature);
    expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));

    // validate
    const result = await options.protocolInterface.verify([await keyStore.get('key', true)], Buffer.from(payload), signature, options);
    expect(result.result).toBeTruthy();

    // Flat serialization
    serialized = options.protocolInterface.serialize(signature, 'JwsFlatJson', options);
    parsed = JSON.parse(serialized);
    expect(parsed['payload']).toBeDefined();
    expect(parsed['protected']).toBeDefined();
    expect(parsed['signature']).toBeDefined();
    
    deserialized = options.protocolInterface.deserialize(serialized, 'JwsFlatJson', options);
    deSignatures = deserialized.get(JoseConstants.tokenSignatures);
    expect(deSignatures[0].protected).toEqual(signatures[0].protected);
    expect(deSignatures[0].signature).toEqual(signatures[0].signature);
    expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));

    // Compact serialization
    serialized = options.protocolInterface.serialize(signature, 'JwsCompactJson', options);
    parsed = serialized.split('.');
    expect(parsed.length).toEqual(3);
    
    deserialized = options.protocolInterface.deserialize(serialized, 'JwsCompactJson', options);
    deSignatures = deserialized.get(JoseConstants.tokenSignatures);
    expect(deSignatures[0].protected).toEqual(signatures[0].protected);
    expect(deSignatures[0].signature).toEqual(signatures[0].signature);
    expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));

    });

    it('should set headers in JwsToken', async () => {
      const payload = 'The true sign of intelligence is not knowledge but imagination.';      
      const keyStore = new KeyStoreInMemory();
      await keyStore.save('seed', new SecretKey('ABEE'));
      const cryptoFactory = new CryptoFactory(keyStore, new SubtleCryptoNodeOperations())
      const options: IPayloadProtectionProtocolOptions = {
          cryptoFactory: cryptoFactory,
          protocolOption: new TSMap<string, any>([
            [JoseConstants.optionHeader,new TSMap([['test', 'ES256K']]) ],
            [JoseConstants.optionProtectedHeader,new TSMap([['test', 'elo']]) ]
        ]),
          protocolInterface: new JoseProtocol()
      };
   
      const alg = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' }, format: 'DER' };
      const generator = new SubtleCryptoExtension(cryptoFactory);
      const privateKey = await generator.generatePairwiseKey(alg, 'seed', 'persona','peer');
      (<any>privateKey).alg = 'ES256K';
      await keyStore.save('key', privateKey);
      const publicKey = await keyStore.get('key', true);

      // sign
      const signature = await options.protocolInterface.sign('key', Buffer.from(payload), 'JwsGeneralJson', options);
      const signatures = signature.get(JoseConstants.tokenSignatures);
      expect(signatures[0].protected.get('test')).toEqual('elo');
      expect(signatures[0].header.get('test')).toEqual('ES256K');
      expect(signatures[0].signature).toBeDefined();
      expect(signature.get(JoseConstants.tokenPayload)).toEqual(Buffer.from(payload));
  
      // serialize
      let serialized = options.protocolInterface.serialize(signature, 'JwsGeneralJson', options);
      let parsed = JSON.parse(serialized);
      expect(parsed['payload']).toBeDefined();
      expect(parsed['signatures']).toBeDefined();
  
      // deserialize
      let deserialized = options.protocolInterface.deserialize(serialized, 'JwsGeneralJson', options);
      let deSignatures = deserialized.get(JoseConstants.tokenSignatures);
      expect(deSignatures[0].protected).toEqual(signatures[0].protected);
      expect(deSignatures[0].header).toEqual(signatures[0].header);
      expect(deSignatures[0].signature).toEqual(signatures[0].signature);
      expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));
  
      // validate
      const result = await options.protocolInterface.verify([await keyStore.get('key', true)], Buffer.from(payload), signature, options);
      expect(result.result).toBeTruthy();
  
      // Flat serialization
      serialized = options.protocolInterface.serialize(signature, 'JwsFlatJson', options);
      parsed = JSON.parse(serialized);
      expect(parsed['payload']).toBeDefined();
      expect(parsed['protected']).toBeDefined();
      expect(parsed['header']).toBeDefined();
      expect(parsed['signature']).toBeDefined();
      
      deserialized = options.protocolInterface.deserialize(serialized, 'JwsFlatJson', options);
      deSignatures = deserialized.get(JoseConstants.tokenSignatures);
      expect(deSignatures[0].protected).toEqual(signatures[0].protected);
      expect(deSignatures[0].header).toEqual(signatures[0].header);
      expect(deSignatures[0].signature).toEqual(signatures[0].signature);
      expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));
  
      // Compact serialization
      serialized = options.protocolInterface.serialize(signature, 'JwsCompactJson', options);
      parsed = serialized.split('.');
      expect(parsed.length).toEqual(3);
      
      deserialized = options.protocolInterface.deserialize(serialized, 'JwsCompactJson', options);
      deSignatures = deserialized.get(JoseConstants.tokenSignatures);
      expect(deSignatures[0].protected).toEqual(signatures[0].protected);
      expect(deSignatures[0].signature).toEqual(signatures[0].signature);
      expect(deserialized.get(JoseConstants.tokenPayload)).toEqual(signature.get(JoseConstants.tokenPayload));
  
      });
  });
