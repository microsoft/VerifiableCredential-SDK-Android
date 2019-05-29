/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../../../Identifier';
import IResolver from '../../../resolvers/IResolver';
import JwsToken from '../jws/JwsToken';

/**
 * Named Arguments of Did Protocol
 */
export interface DidProtocolOptions {
  
  /**
   * Identifier who wants to send requests.
   */
  sender: Identifier;

  /**
   * Resolver for resolving identifier documents
   */
  resolver: IResolver;

}

/**
 * Hub Protocol for decrypting/verifying and encrypting/signing payloads
 */
export default class DidProtocol {

  /**
   * Client's Identifier to be used for signing and decrypting.
   */
  private sender: Identifier;

  /**
   * Resolver for resolving Identifier Documents
   */
  private resolver: IResolver;

  /**
   * Authentication constructor
   * @param options Arguments to a constructor in a named object
   */
  constructor (options: DidProtocolOptions) {
    this.sender = options.sender;
    this.resolver = options.resolver;
  }

  /**
   * Unwrapping method for unwrapping requests/responses.
   * Decrypt a cipher using [PrivateKey] of Client Identifier and then verify payload.
   * @param keyReference key reference of private key in keystore used to decrypt.
   * @param cipher the cipher to be decrypted and verified by client Identifier.
   */
  public async decryptAndVerify(keyReference: string, cipher: Buffer): Promise<any> {
    const jws = await this.sender.decrypt(cipher, keyReference);
    const jwsToken = Jwstoken
  }

  /**
   * Wrapping method for wrapping requests/responses.
   * Sign a payload using [PrivateKey] in client's keystore and encrypt payload using [PublicKey] 
   * @param payload the payload to be signed and encrypted by client Identifier.
   */
  public async signAndEncrypt(keyReference: string, payload: any): Promise<Buffer> {
    const jws = await this.sender.sign(payload, keyReference);
    return this.hub.encrypt(jws);
  }

}
