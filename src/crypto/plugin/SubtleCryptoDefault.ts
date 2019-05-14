/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as webcrypto from 'webcrypto-core';
const SubtleCrypto = webcrypto.SubtleCrypto;

/**
 * Subtle crypto class.
 * Provides support for nodejs and browser
 */
export default class SubtleCryptoDefault extends SubtleCrypto {
  /**
   * Constructs a new instance of the class.
   */
  constructor () {
    super();
  }
}
