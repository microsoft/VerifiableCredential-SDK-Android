/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { Identifier } from '..';

/**
 * Enumeration of the supported credential types.
 */
export enum CredentialType {
  VerifiedCredential = 'https://www.w3.org/2018/credentials/v1',
  SelfIssued = 'https://self-issued.me'
}

/**
 * Interface defining common properties and
 * methods of a credential.
 */
export default interface ICredential {

  /**
   * The identifier the credential was
   * issued to.
   */
  issuedTo: Identifier;

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
}
