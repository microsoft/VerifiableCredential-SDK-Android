import CryptoFactory from "../../src/crypto/plugin/CryptoFactory";
import { KeyStoreInMemory, SubtleCryptoNodeOperations, CryptoHelpers, RsaPublicKey, PublicKey } from "../../src";
import { SubtleCrypto } from "webcrypto-core";
import { KeyType } from "../../src/crypto/keys/KeyTypeFactory";
import { KeyUse } from "../../src/crypto/keys/KeyUseFactory";
import { KeyOperation } from "../../src/crypto/keys/JsonWebKey";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
class TestSubtle extends SubtleCrypto {
  private alg: string;
  constructor (alg: string) {
    super();
    this.alg = alg;
  }

  public getSign(name: string): string {
    return this.alg + name;
  }
}

class TestCrypto extends SubtleCryptoNodeOperations {

 /**
  * Gets all of the message digest Algorithms from the plugin. 
 * @returns a subtle crypto object for message digests
   */
   public getMessageDigests (): SubtleCrypto {
    return this.getCrypto('getMessageDigests');
  }

 /**
  * Gets all of the MAC signing Algorithms from the plugin. 
  * Will be used for primitive operations such as key generation.
 * @returns a subtle crypto object for message signing
   */
   public messageAuthenticationCodeSigners (): SubtleCrypto {
    return this.getCrypto('messageAuthenticationCodeSigners');
  }

/**
 * Returns the @class SubtleCrypto implementation for the nodes environment
 */
  private getCrypto(alg: string): SubtleCrypto {
    return <any> new TestSubtle(alg);
  }
}

 describe('CryptoHelpers', () => {
  it('should getSubtleCryptoForAlgorithm', () => {
    const keyStore = new KeyStoreInMemory();
    const crypto = new TestCrypto();
    const cryptoFactory: CryptoFactory = new CryptoFactory(keyStore, crypto);

    let subtle: any = <any>CryptoHelpers.getSubtleCryptoForAlgorithm(cryptoFactory, { name: 'SHA-512' });
    expect(subtle.getSign('test')).toEqual('getMessageDigeststest');
    subtle = <any>CryptoHelpers.getSubtleCryptoForAlgorithm(cryptoFactory, { name: 'HMAC' });
    expect(subtle.getSign('test')).toEqual('messageAuthenticationCodeSignerstest');

    let throwed = false;
    try {
      CryptoHelpers.getSubtleCryptoForAlgorithm(cryptoFactory, { name: 'SHA' }); 
    } catch (err) {
      throwed = true;
      expect(err.message).toEqual(`Algorithm '{"name":"SHA"}' is not supported`);
    }
    expect(throwed).toBeTruthy();
  });

  it('should getKeyImportAlgorithm', () => {
    const keyStore = new KeyStoreInMemory();
    const crypto = new TestCrypto();
    const cryptoFactory: CryptoFactory = new CryptoFactory(keyStore, crypto);

    const publicKey = {
      kty: KeyType.RSA,
      use: KeyUse.Signature,
      alg: 'RS256',
      kid: '#key1',
      key_ops: [KeyOperation.Verify]
    };
    const key = new RsaPublicKey(<PublicKey>publicKey);

    const algorithm: any = CryptoHelpers.getKeyImportAlgorithm({name: 'SHA-512'}, key);
    expect(algorithm.name).toEqual('SHA-512');

    let throwed = false;
    try {
      CryptoHelpers.getKeyImportAlgorithm({name: 'SHA-888'}, key);
    } catch (err) {
      throwed = true;
      expect(err.message).toEqual(`Algorithm '{"name":"SHA-888"}' is not supported`);
    }
    expect(throwed).toBeTruthy();
  });
 
  it('should jwaToWebCrypto', () => {
    const algorithm: any = CryptoHelpers.jwaToWebCrypto('A192GCM');
    expect(algorithm.name).toEqual('AES-GCM');

    let throwed = false;
    try {
      CryptoHelpers.jwaToWebCrypto('SHA-888');
    } catch (err) {
      throwed = true;
      expect(err.message).toEqual(`Algorithm "SHA-888" is not supported`);
    }
    expect(throwed).toBeTruthy();
  });

  it('should webCryptoToJwa', () => {
    const algorithm: any = CryptoHelpers.webCryptoToJwa({name: 'RSA-OAEP-256'});
    expect(algorithm).toEqual('RSA-OAEP-256');
  });
});
