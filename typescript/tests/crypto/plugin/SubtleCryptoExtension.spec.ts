/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import KeyStoreInMemory from '../../../src/crypto/keyStore/KeyStoreInMemory';
import SubtleCryptoNodeOperations from '../../../src/crypto/plugin/SubtleCryptoNodeOperations';
import { SubtleCryptoExtension, EcPrivateKey, RsaPrivateKey } from '../../../src';

describe('SubtleCryptoExtension', () => {
  it('should sign a message', async() => {
    const keyStore = new KeyStoreInMemory();
    const factory = new CryptoFactory(keyStore, new SubtleCryptoNodeOperations());
    const subtle = new SubtleCryptoExtension(factory);
    const alg = { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' }, format: 'DER' };

    const jwk = new EcPrivateKey({"kid":"#signing","kty":"EC","use":"sig","alg":"ES256K","crv":"P-256K","x":"7RlJnsuYQuSNdpRAFwejCXZqsAccW_QKWw4dPmABBVA","y":"nf0vn9ib6ObyLm4WaDWUe8g3gkEwo2jVbthS7R4MsaU","d":"2PtA4bb6fXprFLfjIJsi5Cer8YAdEDVDomYNYK9ppkU"});
    await keyStore.save('key', jwk);
    const payload = Buffer.from('test');
    const signature = await subtle.signByKeyStore(alg, 'key', payload);
    const result = await subtle.verifyByJwk(alg, await keyStore.get('key', true), signature, payload);
    expect(result).toBeTruthy();
  })
  it('should generate an RSA key via the plugin', async() => {
    const keyStore = new KeyStoreInMemory();
    const factory = new CryptoFactory(keyStore, new SubtleCryptoNodeOperations());
    const generator = factory.getKeyEncrypter('RSA-OAEP');
    const alg = { name: 'RSA-OAEP', hash: 'SHA-256', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]) };
    const key = await generator.generateKey(alg, true, ['encrypt', 'decrypt']);
    let jwk = new RsaPrivateKey(await generator.exportKey('jwk', (<any>key).privateKey));
    expect(jwk.d).toBeDefined();
    expect(jwk.p).toBeDefined();
    expect(jwk.q).toBeDefined();
  })
});
