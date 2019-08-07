/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IVerificationResult from '../IVerificationResult';
import { ICryptoToken } from '../ICryptoToken';
import { IPayloadProtection } from '../IPayloadProtection';
import IPayloadProtectionOptions from '../IPayloadProtectionOptions';
import JwsToken from './jws/JwsToken';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import CryptoProtocolError from '../CryptoProtocolError';
import JoseConstants from './JoseConstants';
import JweToken from './jwe/JweToken';
import PublicKey from '../../keys/PublicKey';
import { IJwsSigningOptions, IJweEncryptionOptions } from './IJoseOptions';

/**
 * Class to implement the JOSE protocol.
 */
export default class JoseProtocol implements IPayloadProtection {

  /**
   * Signs contents using the given private key reference.
   *
   * @param signingKeyReference Reference to the signing key.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in requested format.
   */
   public async sign (signingKeyReference: string, payload: Buffer, format: string, options: IPayloadProtectionOptions): Promise<ICryptoToken> {
    const jwsOptions: IJwsSigningOptions = JwsToken.fromPayloadProtectionOptions(options);
    const token: JwsToken = new JwsToken(jwsOptions);
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    return JwsToken.toCryptoToken(protocolFormat, await token.sign(signingKeyReference, payload, protocolFormat), options);
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
   public async verify (validationKeys: PublicKey[], _payload: Buffer, signature: ICryptoToken, options?: IPayloadProtectionOptions): Promise<IVerificationResult> {
    const jwsOptions: IJwsSigningOptions = JwsToken.fromPayloadProtectionOptions(<IPayloadProtectionOptions>options);
    const token: JwsToken = JwsToken.fromCryptoToken(signature, <IPayloadProtectionOptions>options);
    const result = await token.verify(validationKeys);
    return {
      result: result,
      reason: ''
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
   * @param options used for the encryption. These options override the options provided in the constructor.
   * @returns JweToken with encrypted payload.
   */
   public async encrypt (recipients: PublicKey[], payload: Buffer, format: string, options: IPayloadProtectionOptions): Promise<ICryptoToken> {
    const jweOptions: IJweEncryptionOptions = JweToken.fromPayloadProtectionOptions(options);
    const token: JweToken = new JweToken(jweOptions);
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    return JweToken.toCryptoToken(protocolFormat, await token.encrypt(recipients, payload.toString('utf8'), protocolFormat, jweOptions), options);
   }

   /**
   * Decrypt the content.
   * 
   * @param decryptionKeyReference Reference to the decryption key.
   * @param token The crypto token to decrypt.
   * @param options used for the decryption. These options override the options provided in the constructor.
   * @returns Decrypted payload.
   */
   public async decrypt (decryptionKeyReference: string, token: ICryptoToken, options: IPayloadProtectionOptions): Promise<Buffer> {
    const cipher: JweToken = JweToken.fromCryptoToken(token, options);
    (<any>cipher).options = JweToken.fromPayloadProtectionOptions(options);
    return await cipher.decrypt(decryptionKeyReference);
   }

   /**
   * Serialize a cryptographic token
   * @param token The crypto token to serialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   public serialize (token: ICryptoToken, format: string, options: IPayloadProtectionOptions): string {
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    
    switch (protocolFormat) {
      case ProtectionFormat.JwsFlatJson:
      case ProtectionFormat.JwsCompactJson:
      case ProtectionFormat.JwsGeneralJson:
        const signature: JwsToken = JwsToken.fromCryptoToken(token, options);
        return signature.serialize(protocolFormat);
      case ProtectionFormat.JweFlatJson:
      case ProtectionFormat.JweCompactJson:
      case ProtectionFormat.JweGeneralJson:
        const cipher: JweToken = JweToken.fromCryptoToken(token, options);
        return cipher.serialize(protocolFormat);
    default:
        throw new CryptoProtocolError(JoseConstants.Jose, `Serialization format '${format}' is not supported`);
    }
   }

  /**
   * Deserialize a cryptographic token
   * @param token The crypto token to deserialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   public deserialize (token: string, format: string, options: IPayloadProtectionOptions): ICryptoToken {
    const protocolFormat: ProtectionFormat = this.getProtectionFormat(format);
    switch (protocolFormat) {
      case ProtectionFormat.JwsFlatJson:
      case ProtectionFormat.JwsCompactJson:
      case ProtectionFormat.JwsGeneralJson:
        const jwsProtectOptions = JwsToken.fromPayloadProtectionOptions(options);
        return JwsToken.toCryptoToken(protocolFormat, JwsToken.deserialize(token, jwsProtectOptions), options);
      case ProtectionFormat.JweFlatJson:
      case ProtectionFormat.JweCompactJson:
      case ProtectionFormat.JweGeneralJson:
        const jweProtectOptions = JweToken.fromPayloadProtectionOptions(options);
        return JweToken.toCryptoToken(protocolFormat, JweToken.deserialize(token, jweProtectOptions), options);
      default:
        throw new CryptoProtocolError(JoseConstants.Jose, `Serialization format '${format}' is not supported`);
    }
   }

  /**
   * Deserialize a cryptographic token
   * @param token The crypto token to deserialize.
   * @param options used for the token. These options override the options provided in the constructor.
   */
   public static deserialize (token: string, options?: IPayloadProtectionOptions): ICryptoToken {
    const parts = token.split('.');
    const protocol = new JoseProtocol();

    if (parts.length === 3) {
      const deserializationOptions = options ? JwsToken.fromPayloadProtectionOptions(options) : <IJwsSigningOptions>{};
      return JwsToken.toCryptoToken(ProtectionFormat.JwsCompactJson, JwsToken.deserialize(token, deserializationOptions), <IPayloadProtectionOptions>options);
    } else if (parts.length === 5) {
      const deserializationOptions = options ? JweToken.fromPayloadProtectionOptions(options) : <IJweEncryptionOptions>{};
      return JweToken.toCryptoToken(ProtectionFormat.JweCompactJson, JweToken.deserialize(token, deserializationOptions), <IPayloadProtectionOptions>options);
    }
    const parsed = JSON.parse(token);
    if (parsed[JoseConstants.tokenSignatures] || parsed[JoseConstants.tokenSignature]) {
      const deserializationOptions = options ? JwsToken.fromPayloadProtectionOptions(options) : <IJwsSigningOptions>{};
      return JwsToken.toCryptoToken(parsed[JoseConstants.tokenSignatures] ? ProtectionFormat.JwsGeneralJson : ProtectionFormat.JwsFlatJson, JwsToken.deserialize(token, deserializationOptions), <IPayloadProtectionOptions>options);
    }
    if (parsed[JoseConstants.tokenRecipients] || parsed[JoseConstants.tokenCiphertext]) {
      const deserializationOptions = options ? JweToken.fromPayloadProtectionOptions(options) : <IJweEncryptionOptions>{};
      return JweToken.toCryptoToken(parsed[JoseConstants.tokenRecipients] ? ProtectionFormat.JweGeneralJson : ProtectionFormat.JweFlatJson, JweToken.deserialize(token, deserializationOptions), <IPayloadProtectionOptions>options);
    }

    throw new CryptoProtocolError(JoseConstants.Jose, 'Unrecognised token to deserialize');
   }

   // Map string to protection format
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
