/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IProtocolOptions from "./IProtocolOptions";
import { PublicKey } from "../..";
import IVerificationResult from "./IVerificationResult";
import { ICryptoToken } from "./ICryptoToken";
import { IProtocolInterface } from "../IProtocolInterface";
import IProtocolOptions from "../IProtocolOptions";
import JwsToken from "../jws/JwsToken";
import { ProtectionFormat } from "../../keyStore/ProtectionFormat";
import CryptoProtocolError from "../CryptoProtocolError";
import JoseConstants from "./JoseConstants";
import JweToken from "../jwe/JweToken";

/**
 * Class to implement the JOSE protocol.
 */
export default class JoseProtocol implements IProtocolInterface {
  // Options passed into the ctor
  private options: IProtocolOptions;

  /**
   * Signs contents using the given private key reference.
   *
   * @param signingKeyReference Reference to the signing key.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in requested format.
   */
   public async sign (signingKeyReference: string, payload: Buffer, format: string, options: IProtocolOptions): Promise<ICryptoToken> {
    const token: JwsToken = new JwsToken(options);
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    return token.sign(signingKeyReference, payload, protocolFormat);
   }

  /**
   * Verify the signature.
   *
   * @param validationKeys Public key to validate the signature.
   * @param payload that was signed
   * @param signature on payload  
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns True if signature validated.
   */
   public async verify (validationKeys: PublicKey[], _payload: Buffer, signature: ICryptoToken, options?: IProtocolOptions): Promise<IVerificationResult> {
    const token: JwsToken = <JwsToken>signature;
    (<any>token).options = options;
    const result = await token.verify(validationKeys);
    return {
      result: result
    };
   }

  /**
   * Encrypt content using the given public keys in JWK format.
   * The key type enforces the key encryption algorithm.
   * The options can override certain algorithm choices.
   * 
   * @param recipients List of recipients' public keys.
   * @param payload to encrypt.
   * @param format of the final serialization.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns JweToken with encrypted payload.
   */
   public async encrypt (recipients: PublicKey[], payload: Buffer, format: string, options?: IProtocolOptions): Promise<ICryptoToken> {
    const token: JweToken = new JweToken(options);
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    return token.encrypt(recipients, payload.toString('utf8'), protocolFormat);
   }

  /**
   * Decrypt the content.
   * 
   * @param decryptionKeyReference Reference to the decryption key.
   * @param token The crypto token to decrypt.
   * @param options used for the decryption. These options override the options provided in the constructor.
   * @returns Decrypted payload.
   */
   public async decrypt (decryptionKeyReference: string, token: ICryptoToken, options?: IProtocolOptions): Promise<Buffer> {
    const cipher: JweToken = <JweToken>token;
    (<any>token).options = options;
    return await cipher.decrypt(decryptionKeyReference);
   }

   /**
   * Serialize a cryptographic token
   * @param token The crypto token to serialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   public serialize (token: ICryptoToken, format: string, options?: IProtocolOptions): string {
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    
    switch (protocolFormat) {
      case ProtectionFormat.JwsFlatJson:
      case ProtectionFormat.JwsCompactJson:
      case ProtectionFormat.JwsGeneralJson:
        const signature: JwsToken = <JwsToken>token;
        return signature.serialize(options);
      case ProtectionFormat.JweFlatJson:
      case ProtectionFormat.JweCompactJson:
      case ProtectionFormat.JweGeneralJson:
        const cipher: JweToken = <JweToken>token;
        return cipher.serialize(options);
      default:
        throw new CryptoProtocolError(JoseConstants.Jose, `Serialization format '${format}' is not supported`);
    }
   }

  /**
   * Deserialize a cryptographic token
   * @param token The crypto token to serialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   public deserialize (token: string, format: string, options?: IProtocolOptions): ICryptoToken {
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    
    switch (protocolFormat) {
      case ProtectionFormat.JwsFlatJson:
      case ProtectionFormat.JwsCompactJson:
      case ProtectionFormat.JwsGeneralJson:
        return JwsToken.deserialize(token, options);
      case ProtectionFormat.JweFlatJson:
      case ProtectionFormat.JweCompactJson:
      case ProtectionFormat.JweGeneralJson:
        return JweToken.deserialize(token, options);
      default:
        throw new CryptoProtocolError(JoseConstants.Jose, `Serialization format '${format}' is not supported`);
    }
   }

  private getProtectionFormat(format: string): ProtectionFormat {
    switch(format.toLocaleLowerCase()) {
      case 'jwsflatjson': return ProtectionFormat.JwsFlatJson;
      case 'jwscompactjson': return ProtectionFormat.JwsCompactJson;
      case 'jwsgeneraljson': return ProtectionFormat.JwsGeneralJson;
      case 'jweflatjson': return ProtectionFormat.JweFlatJson;
      case 'jwecompactjson': return ProtectionFormat.JweCompactJson;
      case 'jwegeneraljson': return ProtectionFormat.JweGeneralJson;
      default:
        throw new CryptoProtocolError(JoseConstants.Jose, `Format '${format}' is not supported`);
    }
  }
}
