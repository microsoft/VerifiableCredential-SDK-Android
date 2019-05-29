/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import KeyStoreInMemory from '../../../src/crypto/keyStore/KeyStoreInMemory';

describe('CryptoFactory', () => {
  it('should create a crypto suite',() => {
    const keyStore = new KeyStoreInMemory();
    
    const factory = new CryptoFactory(keyStore);
    expect(factory).toBeDefined();
    const keyEncrypter = factory.getKeyEncrypter('*');
    expect(keyEncrypter).toBeDefined();
    const macSigner = factory.getMessageAuthenticationCodeSigners('*');
    expect(macSigner).toBeDefined();
    const messageDigest = factory.getMessageDigest('*');
    expect(messageDigest).toBeDefined();
    const messageSigner = factory.getMessageSigner('*');
    expect(messageSigner).toBeDefined();
    const sharedKeyEncrypter = factory.getSharedKeyEncrypter('*');
    expect(sharedKeyEncrypter).toBeDefined();
    const symmetricEncrypter = factory.getSymmetricEncrypter('*');
    expect(symmetricEncrypter).toBeDefined();
  })
});
