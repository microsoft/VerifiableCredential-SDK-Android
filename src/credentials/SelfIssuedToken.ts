/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { CredentialType } from './ICrendential';
import Identifier from '../Identifier';
import { JsonWebToken } from './JsonWebToken';

/**
 * JsonWebToken (RFC7519) implementation of a
 * credential.
 */
export class SelfIssuedToken extends JsonWebToken {
  /**
   * @inheritdoc
   */
  public type: CredentialType = CredentialType.SelfIssuedToken;

  /**
   * Constructs an instance of the credential
   * from the provided token string.
   * @param serializedToken the string representation of the token.
   */
  constructor (serializedToken: string) {
    // Try parse the token value
    const token: any = JSON.parse(token);

    // Check if the token has a did claim
    if (token.did) {
      this.issuedBy = new Identifier(token.did);
    }

    if (token.aud) {
      this.owner = new Identifier(token.aud);
    }
  }
}
