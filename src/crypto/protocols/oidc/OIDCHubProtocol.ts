/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../../../Identifier';

/**
 * Named Arguments of Hub Protocol
 */
export interface OIDCHubProtocolOptions {
  /**
   * Hub Identifier.
   */
  hub: Identifier;
  
  /**
   * Client Identifier who wants to contact hub.
   */
  client: Identifier;

}

/**
 * Hub Protocol for decrypting/verifying and encrypting/signing payloads
 */
export default class OIDCHubProtocol {

  /**
   * Client's Identifier to be used for signing and decrypting.
   */
  private client: Identifier;

  /**
   * Hub's identifier to be used for encrypting and verifying.
   */
  private hub: Identifier;

  /**
   * Authentication constructor
   * @param options Arguments to a constructor in a named object
   */
  constructor (options: OIDCHubProtocolOptions) {
    this.client = options.client;
    this.hub = options.hub;
  }

  /**
   * Decrypt a cipher using [PrivateKey] of Client Identifier and then verify payload.
   * @param keyReference key reference of private key in keystore used to decrypt.
   * @param cipher the cipher to be decrypted and verified by client Identifier.
   */
  public async decryptAndVerify(keyReference: string, cipher: Buffer): Promise<any> {
    const jws = await this.client.decrypt(cipher, keyReference);
    return this.hub.verify(jws);
  }

  /**
   * Sign a payload using [PrivateKey] in client's keystore and encrypt payload using [PublicKey] 
   * @param payload the payload to be signed and encrypted by client Identifier.
   */
  public async signAndEncrypt(keyReference: string, payload: any): Promise<Buffer> {
    const jws = await this.client.sign(payload, keyReference);
    return this.hub.encrypt(jws);
  }

}
