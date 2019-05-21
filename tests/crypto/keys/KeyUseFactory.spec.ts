// tslint:disable-next-line: import-name
import KeyUseFactory, { KeyUse } from '../../../src/crypto/keys/KeyUse';

describe('KeyUseFactory', () => {
  it(`should return the key use of signature for 'hmac'`, () => {
    const alg = { name: 'hmac' };
    expect(KeyUseFactory.create(alg)).toBe(KeyUse.Signature);
  });

  it(`should return the key use of signature for 'ecdsa'`, () => {
    const alg = { name: 'ecdsa' };
    expect(KeyUseFactory.create(alg)).toBe(KeyUse.Signature);
  });

  it(`should return the key use of encryption for 'ecdh'`, () => {
    const alg = { name: 'ecdh' };
    expect(KeyUseFactory.create(alg)).toBe(KeyUse.Encryption);
  });

  it(`should return the key use of signature for 'rsassa-pkcs1-v1_5'`, () => {
    const alg = { name: 'rsassa-pkcs1-v1_5' };
    expect(KeyUseFactory.create(alg)).toBe(KeyUse.Signature);
  });

  it('should throw on unsupported algorithm', () => {
    const alg = { name: 'xxx' };
    expect(() => KeyUseFactory.create(alg)).toThrowError(`The algorithm 'xxx' is not supported`);
  });
});
