/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Multihash from '../../src/registrars/Multihash';

describe('Multihash', () => {
  it('should throw on unsupported hash algorithm', () => {
    const buffer = Buffer.from('Some content');
    expect(() => Multihash.hash(buffer, 99)).toThrowError(`Hashing algorithm '99' not supported.`);
  });

  it('should return expected multihash', () => {
    const buffer = Buffer.from('some document content');
    const hash = Multihash.hash(buffer, 18);
    expect(hash).toBeDefined();
    expect(hash[0]).toEqual(18);
    expect(hash[1]).toEqual(32);
    expect(hash.length).toEqual(34);
  });
});

