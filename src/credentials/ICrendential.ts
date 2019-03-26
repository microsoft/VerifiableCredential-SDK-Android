/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { Identifier } from '..';

/**
 * Interface defining common properties and
 * methods of a credential.
 */
export default interface ICrendential {
  /**
   * The identifier the credential was
   * issued to.
   */
  owner: Identifier;

  /**
   * The identifier of the issuer of
   * the credential.
   */
  issuedBy: Identifier;

  /**
   * The date the credential was issued.
   */
  issuedAt: Date;

  /**
   * The date and time that the
   * credential expires at.
   */
  expiresAt?: Date;

  /**
   * A boolean indicating whether the
   * credential was self issued.
   */
  isSelfIssued: boolean;

  /**
   * A boolean indicating whether the
   * credential has been signed by the
   * issuer.
   */
  isSigned: boolean;
}
