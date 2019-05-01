/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from './ICredential';
import Identifier from '../Identifier';

/**
 * Implementation of an OpenID Connect
 * self-issued id token.
 * @implements ICredential
 */
export class VerifiedCredential implements ICredential {
  /**
   * The identifier of the issuer of
   * the credential.
   */
  public readonly issuedBy: Identifier;

  /**
   * The identifier the credential was
   * issued to.
   */
  public readonly issuedTo: Identifier;

  /**
   * The date the credential was issued.
   */
  public readonly issuedAt: Date;

  /**
   * The date and time that the
   * credential expires at.
   */
  public readonly expiresAt?: Date;

  /**
   * Constructs a new instance of a verified
   * credential for the specified identifier.
   * @param issuedBy the specified identifier.
   * @param issuedTo the specified identifier.
   * @param issuedAt date and time.
   */
  constructor (issuedBy: Identifier, issuedTo: Identifier, issuedAt: Date) {
    this.issuedBy = issuedBy;
    this.issuedTo = issuedTo;
    this.issuedAt = issuedAt;
  }
}
