/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { SubtleCryptoElliptic } from '@microsoft/useragent-plugin-secp256k1';
import SubtleCryptoOperations from './SubtleCryptoOperations';
import { SubtleCrypto } from 'webcrypto-core';

/**
 * Default crypto suite implementing the default plugable crypto layer
 *  */
export default class SubtleCryptoBrowserOperations extends SubtleCryptoOperations {

  constructor () {
    super();
  }

 /**
  * Gets all of the message signing Algorithms from the plugin
  * @returns a subtle crypto object for message signing
  */
  public getMessageSigners (): SubtleCrypto {
    return <SubtleCrypto> new SubtleCryptoElliptic();
  }
}
