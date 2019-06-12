/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from "base64url";
import JwsToken from "../../../src/crypto/protocols/jws/JwsToken";
import SecretKey from "../../../src/crypto/keys/SecretKey";
import KeyStoreInMemory from "../../../src/crypto/keyStore/KeyStoreInMemory";
import CryptoFactory from "../../../src/crypto/plugin/CryptoFactory";
import SubtleCryptoNodeOperations from "../../../src/crypto/plugin/SubtleCryptoNodeOperations";
import { ProtectionFormat } from "../../../src/crypto/keyStore/ProtectionFormat";
import { SubtleCryptoExtension } from "../../../src";
import { SubtleCryptoElliptic } from '@microsoft/useragent-plugin-secp256k1';
import { ISigningOptions } from "../../../src/crypto/keyStore/IKeyStore";

describe('JwsToken', () => {
  it('should create a jws token', async () => {
    const payload = 'test payload';
    const keyStore = new KeyStoreInMemory();
    const seedReference = 'seed';
    await keyStore.save(seedReference, new SecretKey('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'));
    const cryptoSuite = new SubtleCryptoNodeOperations();
    const options: ISigningOptions = {
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
});
