/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICrendential, { CredentialType } from './ICrendential';
import Identifier from '../Identifier';

/**
 * JsonWebToken (RFC7519) implementation of a
 * credential.
 */
export class JsonWebToken implements ICrendential {
  /**
   * @inheritdoc
   */
  public type: CredentialType = CredentialType.JsonWebToken;

  /**
   * @inheritdoc
   */
  public owner: Identifier | string | undefined;

  /**
   * @inheritdoc
   */
  public issuedBy: Identifier | string;

  /**
   * @inheritdoc
   */
  public issuedAt: Date;

  /**
   * @inheritdoc
   */
  public expiresAt?: Date;

  /**
   * @inheritdoc
   */
  public isSelfIssued: boolean;

  /**
   * @inheritdoc
   */
  public isSigned: boolean;

  /**
   * Constructs an instance of the credential
   * from the provided token string.
   * @param serializedToken the string representation of the token.
   */
  constructor (serializedToken: string) {
    // Try parse the token value
    const token: any = JSON.parse(token);

/*     iss
    REQUIRED. Issuer Identifier for the Issuer of the response. The iss value is a case sensitive URL using the https scheme that contains scheme, host, and optionally, port number and path components and no query or fragment components.
    sub
    REQUIRED. Subject Identifier. A locally unique and never reassigned identifier within the Issuer for the End-User, which is intended to be consumed by the Client, e.g., 24400320 or AItOawmwtWwcT0k51BayewNvutrJUqsvl6qs7A4. It MUST NOT exceed 255 ASCII characters in length. The sub value is a case sensitive string.
    aud
    REQUIRED. Audience(s) that this ID Token is intended for. It MUST contain the OAuth 2.0 client_id of the Relying Party as an audience value. It MAY also contain identifiers for other audiences. In the general case, the aud value is an array of case sensitive strings. In the common special case when there is one audience, the aud value MAY be a single case sensitive string.
    exp
    REQUIRED. Expiration time on or after which the ID Token MUST NOT be accepted for processing. The processing of this parameter requires that the current date/time MUST be before the expiration date/time listed in the value. Implementers MAY provide for some small leeway, usually no more than a few minutes, to account for clock skew. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time. See RFC 3339 [RFC3339] for details regarding date/times in general and UTC in particular.
    iat
    REQUIRED. Time at which the JWT was issued. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time.
 */

    // Check if the token has a did claim
    if (token.did) {
      this.issuedBy = new Identifier(token.did);
    }

    if (token.aud) {
      this.owner = new Identifier(token.aud);
    }
  }
}
