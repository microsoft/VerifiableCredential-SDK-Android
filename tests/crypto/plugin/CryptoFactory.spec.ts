/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import DefaultCryptoSuite from '../../../src/crypto/plugin/DefaultCryptoSuite';

describe('CryptoFactory', () => {
  it('should create a crypto suite',() => {
    const factory = new CryptoFactory([new DefaultCryptoSuite]);
    expect(factory).toBeDefined();
  })
});