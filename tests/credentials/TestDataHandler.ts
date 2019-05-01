/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IDataHandler from '../../src/credentials/IDataHandler';
import ICredential from '../../src/credentials/ICredential';

/**
 * Implementation of a data handler for testing.
 * @class
 * @implements IDataHandler
 */
export default class TestDataHandler implements IDataHandler {

  /**
   * Test method that simply switches issuedBy and issuedAt fields.
   * @inheritdoc
   */
  public async process (inputCredential: ICredential): Promise<ICredential> {

    const credential: ICredential = {
      issuedBy: inputCredential.issuedTo,
      issuedTo: inputCredential.issuedBy,
      issuedAt: new Date()
    };
    return credential;
  }
}
