
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JweHeader } from "../jwe/IJweGeneralJson";
import { JwsHeader } from "../jws/IJwsGeneralJson";
import base64url from 'base64url';
import { IEncryptionOptions, ISigningOptions } from "../../keyStore/IKeyStore";

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
  public static encodeHeader(header: JweHeader | JwsHeader, toBase64Url: boolean = true): string {
    const serializedHeader = JSON.stringify(header.toJSON());
    if (toBase64Url) {
      return base64url.encode(serializedHeader);
    }
    return serializedHeader;
  }

  /**
   * Get the Protected to be used from the options
   * @param propertyName Property name in options
   * @param initialOptions The initial set of options
   * @param overrideOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  public static getOptionsProperty<T>(propertyName: string, initialOptions?: IEncryptionOptions | ISigningOptions, overrideOptions?: IEncryptionOptions | ISigningOptions,  manadatory: boolean = true): T {
    let overrideOption: T | undefined;
    let initialOption: T | undefined;

    if (overrideOptions) {
      overrideOption = <T>overrideOptions[propertyName];
    }
    if (initialOptions) {
      initialOption = <T>initialOptions[propertyName];
    }

    if (manadatory && !overrideOption && !initialOption) {
      throw new Error(`The property ${propertyName} is missing from options`);
    }

    return overrideOption || <T>initialOption;
  }

}
