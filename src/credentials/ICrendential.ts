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
  JsonWebToken = 'https://tools.ietf.org/html/rfc7519',
  SelfIssuedToken = 'https://self-issued.me'
}

/**
 * Interface defining common properties and
 * methods of a credential.
 */
export default interface ICrendential {

  /**
   * The type of the credential.
   */
  type: CredentialType;

  /**
   * The identifier the credential was
   * issued to.
   */
  owner: Identifier | string | undefined;

  /**
   * The identifier of the issuer of
   * the credential.
   */
  issuedBy: Identifier | string | undefined;

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
