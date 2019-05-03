/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from 'src/credentials/ICredential';

/**
 * Interface defining common properties and
 * methods of a credential.
 */
export default interface IDataHandler {

  /**
   * Process a verifiedCredential and exchange with a self-issued Credential.
   * @param inputCredential Credential to be exchanged.
   */
  process (inputCredential: ICredential): Promise<ICredential>;
}
