import { PublicKey, RsaPublicKey } from "../../../src";
import { KeyType } from "../../../src/crypto/keys/KeyTypeFactory";
import { KeyUse } from "../../../src/crypto/keys/KeyUseFactory";
import { KeyOperation } from "../../../src/crypto/keys/PublicKey";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 
 describe('PublicKey', () => {
  it('should create an instance of a PublicKey', () => {
    const publicKey = {
      kty: KeyType.RSA,
      use: KeyUse.Signature,
      alg: 'RS256',
      kid: '#key1',
      key_ops: [KeyOperation.Verify]
    };
    const key = new RsaPublicKey(<PublicKey>publicKey);
    expect(key.kty).toEqual(KeyType.RSA);
    expect(key.use).toEqual(KeyUse.Signature);
    expect(key.kty).toEqual(KeyType.RSA);
    expect(key.alg).toEqual('RS256');
    expect(key.kid).toEqual('#key1');
    expect(key.key_ops).toEqual([KeyOperation.Verify]);
  });
 });