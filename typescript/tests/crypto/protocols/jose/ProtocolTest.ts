/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { IPayloadProtection } from "../../../../src/crypto/protocols/IPayloadProtection";
import { ProtectionFormat } from "../../../../src/crypto/keyStore/ProtectionFormat";
import JoseConstants from "../../../../src/crypto/protocols/jose/JoseConstants";
import IPayloadProtectionOptions from "../../../../src/crypto/protocols/IPayloadProtectionOptions";
import IVerificationResult from "../../../../src/crypto/protocols/IVerificationResult";
import { PublicKey } from "../../../../src";
import JoseToken from "../../../../src/crypto/protocols/jose/JoseToken";
import { ICryptoToken } from "../../../../src/crypto/protocols/ICryptoToken";

export default class ProtocolTest implements IPayloadProtection {
  public sign (_signingKeyReference: string, payload: Buffer, _format: string, options?: IPayloadProtectionOptions): Promise<ICryptoToken> {
    return new Promise(resolve => {
      const signature = new JoseToken(<IPayloadProtectionOptions>options, new ProtocolTest(), [
      [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
      [JoseConstants.tokenPayload, payload.toString()],
      [JoseConstants.tokenProtected, 'test'],
      [JoseConstants.tokenSignatures, ['test']]]);
    resolve(signature);
    });
  }

  public verify (_validationKeys: PublicKey[], _payload: Buffer, _signature: ICryptoToken, _options?: IPayloadProtectionOptions): Promise<IVerificationResult> {
    return new Promise(resolve => {
      resolve({result: true, reason:'' });
    });
  }

  public encrypt (_recipients: PublicKey[], _payload: Buffer, _format: string, options?: IPayloadProtectionOptions): Promise<ICryptoToken> {
    return new Promise(resolve => {
      const cipher = new JoseToken(<IPayloadProtectionOptions>options, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JweFlatJson],
      ]);
      resolve(cipher);
    });
  }

  public decrypt (_decryptionKeyReference: string, _cipher: ICryptoToken, _options?: IPayloadProtectionOptions): Promise<Buffer> {
    return new Promise(resolve => {
      resolve(Buffer.from(''));
    });
  }

  public serialize (token: ICryptoToken, _format: string, _options?: IPayloadProtectionOptions): string {
    return JSON.stringify(token);
  }

  public deserialize (_token: string, format: string, options?: IPayloadProtectionOptions): ICryptoToken {
    return new JoseToken(<IPayloadProtectionOptions>options, new ProtocolTest(), [
      [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
      [JoseConstants.tokenPayload, format],
      [JoseConstants.tokenProtected, 'test'],
      [JoseConstants.tokenSignatures, ['test']]]);
  }
}
