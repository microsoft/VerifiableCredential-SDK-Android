/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as crypto from 'crypto';
const multihashes = require('multihashes');

/**
 * Class that performs hashing operations using the multihash format.
 */
export default class Multihash {
  /**
   * Hashes the content using the hashing algorithm specified by the latest protocol version.
   */
  public static hash (content: Buffer, hashAlgorithmInMultihashCode: number): Buffer {
    const hashAlgorithm = hashAlgorithmInMultihashCode;

    let hash;
    switch (hashAlgorithm) {
      case 18: // SHA256
        hash = crypto.createHash('sha256').update(content).digest();
        break;
      default:
        throw new Error(`Hashing algorithm '${hashAlgorithm}' not supported.`);
    }
    const hashAlgorithmName = multihashes.codes[hashAlgorithm];
    return multihashes.encode(hash, hashAlgorithmName);
  }
}