/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 
// tslint:disable-next-line: import-name
import KeyTypeFactory, { KeyType } from '../../../src/crypto/keys/KeyTypeFactory';

describe('KeyTypeFactory', () => {
  it(`should return the key type for 'hmac'`, () => {
    const alg = { name: 'hmac' };
    expect(KeyTypeFactory.create(alg)).toBe(KeyType.Oct);
  });

  it(`should return the key type for 'ecdsa'`, () => {
    const alg = { name: 'ecdsa' };
    expect(KeyTypeFactory.create(alg)).toBe(KeyType.EC);
  });

  it(`should return the key type for 'ecdh'`, () => {
    const alg = { name: 'ecdh' };
    expect(KeyTypeFactory.create(alg)).toBe(KeyType.EC);
  });

  it(`should return the key type for 'rsassa-pkcs1-v1_5'`, () => {
    const alg = { name: 'rsassa-pkcs1-v1_5' };
    expect(KeyTypeFactory.create(alg)).toBe(KeyType.RSA);
  });

  it('should throw on unsupported algorithm', () => {
    const alg = { name: 'xxx' };
    expect(() => KeyTypeFactory.create(alg)).toThrowError(`The algorithm 'xxx' is not supported`);
  });
});
