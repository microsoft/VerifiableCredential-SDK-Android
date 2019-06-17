/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JweHeader } from './jwe/IJweGeneralJson';
import { JwsHeader } from './jws/IJwsGeneralJson';
import base64url from 'base64url';
import { IJweEncryptionOptions, IJwsSigningOptions } from '../../protocols/jose/IJoseOptions';
import JoseConstants from './JoseConstants';
import CryptoProtocolError from '../CryptoProtocolError';

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

    return Object.keys(header).length > 0;
  }

  /**
   * Encode the header to JSON and base 64 url.
   * The Typescript Map construct does not allow for JSON.stringify returning {}.
   * TSMap.toJSON prepares a map so it can be serialized as a dictionary.
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
   * @param [initialOptions] The initial set of options
   * @param [overrideOptions] Options passed in after the constructure
   * @param [mandatory] True if property is required
   */
  public static getOptionsProperty<T>(
    propertyName: string,
    initialOptions?: IJweEncryptionOptions | IJwsSigningOptions,
    overrideOptions?: IJweEncryptionOptions | IJwsSigningOptions,
    mandatory: boolean = true
  ): T {
    let overrideOption: T | undefined;
    let initialOption: T | undefined;

    if (overrideOptions) {
      overrideOption = <T>overrideOptions[propertyName];
    }
    if (initialOptions) {
      initialOption = <T>initialOptions[propertyName];
    }

    if (mandatory && !overrideOption && !initialOption) {
      throw new CryptoProtocolError(JoseConstants.Jose, `The property '${propertyName}' is missing from options`);
    }

    return overrideOption || <T>initialOption;
  }
}
