import { JweHeader } from "../jwe/IJweGeneralJson";
import { JwsHeader } from "../jws/IJwsGeneralJson";
import base64url from 'base64url';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Crypto helpers support for plugable crypto layer
 */
export default class JoseHelpers {

  /**
   * Return true if the header has elements
   * @param header to test
   */
  public static headerHasElements(header: JweHeader | JwsHeader | undefined): boolean {
    if (!header) {
      return false;
    }
    return header.length > 0;
  }

  /**
   * Encode the header to JSON and base 64 url
   * @param header to encode
   * @param toBase64Url is true when result needs to be base 64 url
   */
  private static encodeHeader(header: JweHeader | JwsHeader, toBase64Url: boolean = true): string {
    const serializedHeader = JSON.stringify(header.toJSON());
    if (toBase64Url) {
      return base64url.encode(serializedHeader);
    }
    return serializedHeader;
  }

}
