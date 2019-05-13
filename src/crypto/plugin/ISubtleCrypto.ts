/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { SubtleCrypto } from 'webcrypto-core'

/**
 * Interface for the Crypto Algorithms Plugins
 */
export default interface ISubtleCrypto extends SubtleCrypto {
  /**
   * Sign with a key referenced in the key store
   * @param algorithm used for signature
   * @param keyReference points to key in the key store
   * @param data to sign
   */
  signByKeyStore(algorithm: string | RsaPssParams | EcdsaParams | AesCmacParams, keyReference: string, data: BufferSource): PromiseLike<ArrayBuffer>;        
}
