/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from 'src/credentials/ICredential';
import Identifier from 'src/Identifier';

/**
 * Implementation of an OpenID Connect
 * self-issued id token.
 * @implements ICredential
 */
export class VerifiedCredential implements ICredential {
  /**
   * The identifier the credential was
   * issued to.
   * @inheritdoc
   */
  public readonly issuedBy: Identifier;

  /**
   * The identifier of the issuer of
   * the credential.
   * @inheritdoc
   */
  public readonly issuedTo: Identifier;

  /**
   * The date the credential was issued.
   * @inheritdoc
   */
  public readonly issuedAt: Date;

  /**
   * The date and time that the
   * credential expires at.
   * @inheritdoc
   */
  public readonly expiresAt?: Date;

  constructor (issuedBy: Identifier, issuedTo: Identifier, issuedAt: Date) {
    this.issuedBy = issuedBy;
    this.issuedTo = issuedTo;
    this.issuedAt = issuedAt;
  }
}
