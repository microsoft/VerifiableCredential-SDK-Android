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

fdescribe('PairwiseKey EC', () => {

  const KeyGenerationAlgorithm_HMAC256 = 0;
  const KeyGenerationAlgorithm_ECDSA =  1;

  const supportedKeyGenerationAlgorithms = [
    { name: 'HMAC', hash: 'SHA-256' },
    { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' } }
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
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  afterEach(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });

  it(`should throw because the algorithm is not supported for pairwise key generation`, async () => {
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_HMAC256];
    let throwed = false;
    await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, 'did:persona', 'did:peer')    
    .catch((err: any) => {
      throwed = true;
      expect(`Pairwise key for type 'oct' is not supported.`).toBe(err.message);
    });
    expect(throwed).toBeTruthy();
  });
  it('should generate an EC pairwise identifier', async () => {
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    const persona = 'did:persona:1';
    const peer = 'did:peer:1';
    const pairwiseKey1: PrivateKey = await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona, peer);

    // return the same
    const pairwiseKey2: PrivateKey = await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona, peer);
    expect(pairwiseKey1.getPublicKey()).toEqual(pairwiseKey2.getPublicKey());
  });

  it('should generate unique pairwise identifiers for different personas', async () => {
    const results: string[] = [];
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    const persona = 'did:persona:1';
    const peer = 'did:peer:1';
    for (let index = 0 ; index < 1000; index++) {
      const pairwiseKey: EcPrivateKey = <EcPrivateKey> await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, `${persona}-${index}`, peer);
      results.push(<string>pairwiseKey.d);
      expect(1).toBe(results.filter(element => element === pairwiseKey.d).length);
    }
  });

  it('should generate unique pairwise identifiers using a different seed', async () => {
    const results: string[] = [];
    const alg = supportedKeyGenerationAlgorithms[KeyGenerationAlgorithm_ECDSA];
    const persona = 'did:persona:1';
    const peer = 'did:peer:1';
    for (let index = 0 ; index < 1000; index++) {
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
    for (let index = 0 ; index < 1000; index++) {
      const pairwiseKey: EcPrivateKey = <EcPrivateKey> await subtleCryptoExtensions.generatePairwiseKey(<any>alg, seedReference, persona,`${peer}-${index}`);
      results.push(<string>pairwiseKey.d);
      expect(1).toBe(results.filter(element => element === pairwiseKey.d).length);
    }
  });
});
