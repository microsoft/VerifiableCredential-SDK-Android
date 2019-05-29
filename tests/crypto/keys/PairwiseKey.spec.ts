/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 
// tslint:disable-next-line: import-name
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import KeyStoreInMemory from '../../../src/crypto/keyStore/KeyStoreInMemory';
import SubtleCryptoOperations from '../../../src/crypto/plugin/SubtleCryptoOperations';
import SubtleCryptoExtension from '../../../src/crypto/plugin/SubtleCryptoExtension';
import PrivateKey from '../../../src/crypto/keys/PrivateKey';
import EcPrivateKey from '../../../src/crypto/keys/ec/EcPrivateKey';


class Helpers {
  // Make sure we generate the same pairwise key
  public static async generateSamePairwise (subtleCryptoExtensions: SubtleCryptoExtension, seedReference: string, alg: any, persona: string, peer: string) {
    // Generate key
      const pairwiseKey1: PrivateKey = await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona, peer);
  
      // return the same
      const pairwiseKey2: PrivateKey = await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona, peer);
      expect(pairwiseKey1.getPublicKey()).toEqual(pairwiseKey2.getPublicKey());
    }
  
    // Make sure the pairwise key is unique
    public static async generateUniquePairwise (subtleCryptoExtensions: SubtleCryptoExtension, seedReference: string, alg: any, persona: string, peer: string) {
      const results: string[] = [];
      for (let index = 0 ; index < 100; index++) {
        const pairwiseKey: EcPrivateKey = <EcPrivateKey> await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, `${persona}-${index}`, peer);
        results.push(<string>pairwiseKey.d);
        expect(1).toBe(results.filter(element => element === pairwiseKey.d).length);
      }
    }
    
        
}


describe('PairwiseKey', () => {

  //const KeyGenerationAlgorithm_RSA256 = 0;
  const KeyGenerationAlgorithm_ECDSA =  1;

  // tslint:disable-next-line:mocha-no-side-effect-code
  const supportedKeyGenerationAlgorithms = [
      { name: 'RSASSA-PKCS1-v1_5', modulusLength: 1024, publicExponent: new Uint8Array([0x01, 0x00, 0x01]), hash: { name: 'SHA-256' } },
      { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } }
    ];

  const unsupportedKeyGenerationAlgorithms = [
      { name: 'HMAC', hash: 'SHA-256' }
    ];

  // Default Crypto factory
  let keyStore: KeyStoreInMemory;
  let defaultCryptoFactory: CryptoFactory;
  let subtleCryptoExtensions:SubtleCryptoExtension;

  const seedReference = 'masterkey';

  let originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
  beforeEach(() => {
  // Default Crypto factory
  keyStore = new KeyStoreInMemory();
  keyStore.save(seedReference, Buffer.from('abcdefghijklmnop'));
  const cryptoOperations = new SubtleCryptoOperations();
  defaultCryptoFactory = new CryptoFactory(keyStore, cryptoOperations);
  subtleCryptoExtensions = new SubtleCryptoExtension(defaultCryptoFactory);
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
  });

  afterEach(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });

  it(`should throw because the algorithm is not supported for pairwise key generation`, async () => {
    const alg = unsupportedKeyGenerationAlgorithms[0];
    let throwed = false;
    await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, 'did:persona', 'did:peer')    
    .catch((err: any) => {
      throwed = true;
      expect(`Pairwise key for type 'oct' is not supported.`).toBe(err.message);
    });
    expect(throwed).toBeTruthy();
  });

  it('should generate a deterministic pairwise key capable of signing', async () => {
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    // Generate key
    const pairwiseKey1: PrivateKey = await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, 'did:persona', 'did:peer');
    keyStore.save('key', pairwiseKey1);
    const data = Buffer.from('1234567890');
    const signature = await subtleCryptoExtensions.signByKeyStore(alg, 'key', Buffer.from('1234567890'));
    const verify = await subtleCryptoExtensions.verifyByJwk(alg, pairwiseKey1.getPublicKey(), signature, data);
    expect(verify).toBeTruthy();
  });

  it('should generate a deterministic pairwise key', async () => {
    supportedKeyGenerationAlgorithms.forEach(async (alg) => {
      const persona = 'did:persona:1';
      const peer = 'did:peer:1';
      await Helpers.generateSamePairwise(subtleCryptoExtensions, seedReference, alg, persona, peer);
    });
  });

  it('should generate unique pairwise keys for different personas', async () => {
    
    supportedKeyGenerationAlgorithms.forEach(async (alg) => {
      const persona = 'did:persona:1';
      const peer = 'did:peer:1';
      await Helpers.generateUniquePairwise(subtleCryptoExtensions, seedReference, alg, persona, peer);
    });
  });

  it('should generate unique pairwise identifiers using a different seed', async () => {
    const results: string[] = [];
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    const persona = 'did:persona:1';
    const peer = 'did:peer:1';
    for (let index = 0 ; index < 100; index++) {
      const keyReference = `key-${index}`;
      const keyValue = Buffer.from(`1234567890-${index}`);
      keyStore.save(keyReference, keyValue);
      const pairwiseKey: EcPrivateKey = <EcPrivateKey> await subtleCryptoExtensions.generatePairwiseKey(<any>alg, keyReference, persona, peer);
      results.push(<string>pairwiseKey.d);
      expect(1).toBe(results.filter(element => element === pairwiseKey.d).length);
    }
  });

  it('should generate unique pairwise identifiers for different peers', async () => {
    const results: string[] = [];
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    const persona = 'did:persona:1';
    const peer = 'did:peer:1';
    for (let index = 0 ; index < 100; index++) {
      const pairwiseKey: EcPrivateKey = <EcPrivateKey> await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona,`${peer}-${index}`);
      results.push(<string>pairwiseKey.d);
      expect(1).toBe(results.filter(element => element === pairwiseKey.d).length);
    }
  });
});
