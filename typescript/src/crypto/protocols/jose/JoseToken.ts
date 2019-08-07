/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { ICryptoToken } from '../ICryptoToken';
import { TSMap } from 'typescript-map';
import JoseConstants from './JoseConstants';
import JoseProtocol from './JoseProtocol';
import IPayloadProtectionOptions from '../IPayloadProtectionOptions';
import CryptoProtocolError from '../CryptoProtocolError';
import { IPayloadProtection } from '../IPayloadProtection';

/**
 * Class to model JOSE tokens
 */
export default class JoseToken extends TSMap<string, any> implements ICryptoToken {
  private payloadProtection: IPayloadProtection;
  private payloadProtectionProtocolOptions: IPayloadProtectionOptions;

  constructor(payloadProtectionProtocolOptions: IPayloadProtectionOptions, payloadProtection?:IPayloadProtection, inputMap?: any[][]) {
    super(inputMap);
    this.payloadProtectionProtocolOptions = payloadProtectionProtocolOptions;
    this.payloadProtection = payloadProtection || new JoseProtocol();
  }

  /**
   * get the format of the crypto token
   */
  public tokenFormat(): string {
    let format = this.get(JoseConstants.tokenFormat);
    if (!format) {
      throw new CryptoProtocolError('JOSE', `The token format is not found`);
    }

    return format.toString();
  }

  /**
   * Serialize a a @interface ICryptoToken 
   */
   public serialize(): string {
    return this.payloadProtection.serialize(this, this.tokenFormat(), this.payloadProtectionProtocolOptions);
  }

  /**
   * Convert token to json 
   */
   public toJson(): any {
     this.toJSON();
   }
  }
