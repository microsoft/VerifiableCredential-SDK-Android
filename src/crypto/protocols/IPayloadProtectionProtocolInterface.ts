/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IPayloadProtectionProtocolOptions from "./IPayloadProtectionProtocolOptions";
import { PublicKey } from "../..";
import IVerificationResult from "./IVerificationResult";
import { ICryptoToken } from "./ICryptoToken";

/**
 * Interface defining the implementation of the selected protocol.
 */
export interface IPayloadProtectionProtocolInterface {

  /**
   * Signs contents using the given private key reference.
   *
   * @param signingKeyReference Reference to the signing key.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in requested format.
   */
   sign (signingKeyReference: string, payload: Buffer, format: string, options?: IPayloadProtectionProtocolOptions): Promise<ICryptoToken>;

  /**
   * Verify the signature.
   *
   * @param validationKeys Public key to validate the signature.
   * @param payload that was signed
   * @param signature on payload  
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns True if signature validated.
   */
   verify (validationKeys: PublicKey[], payload: Buffer, signature: ICryptoToken, options?: IPayloadProtectionProtocolOptions): Promise<IVerificationResult>;

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
   encrypt (recipients: PublicKey[], payload: Buffer, format: string, options?: IPayloadProtectionProtocolOptions): Promise<ICryptoToken>;

  /**
   * Decrypt the content.
   * 
   * @param decryptionKeyReference Reference to the decryption key.
   * @param token The crypto token to decrypt.
   * @param options used for the decryption. These options override the options provided in the constructor.
   * @returns Decrypted payload.
   */
   decrypt (decryptionKeyReference: string, cipher: ICryptoToken, options?: IPayloadProtectionProtocolOptions): Promise<Buffer>;

  /**
   * Serialize a cryptographic token
   * @param token The crypto token to serialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   serialize (token: ICryptoToken, format: string, options?: IPayloadProtectionProtocolOptions): string;

  /**
   * Deserialize a cryptographic token
   * @param token The crypto token to serialize.
   * @param format Specify the serialization format. If not specified, use default format.
   * @param options used for the decryption. These options override the options provided in the constructor.
   */
   deserialize (token: string, format: string, options?: IPayloadProtectionProtocolOptions): ICryptoToken;
}
