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
   * @inheritdoc
   */
  public readonly issuedBy: Identifier;

  /**
   * @inheritdoc
   */
  public readonly issuedTo: Identifier;

  /**
   * @inheritdoc
   */
  public readonly issuedAt: Date;

  /**
   * @inheritdoc
   */
  public readonly expiresAt?: Date;

  constructor (issuedBy: Identifier, issuedTo: Identifier, issuedAt: Date) {
    this.issuedBy = issuedBy;
    this.issuedTo = issuedTo;
    this.issuedAt = issuedAt;
  }
}
