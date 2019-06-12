/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../../../Identifier';
import IResolver from '../../../resolvers/IResolver';
import JwsToken from '../jws/JwsToken';
import CryptoFactory from '../../plugin/CryptoFactory';
import KeyStoreInMemory from '../../keyStore/KeyStoreInMemory';
import UserAgentOptions from '../../../UserAgentOptions';
import CryptoOptions from '../../../CryptoOptions';
import IKeyStore, { ISigningOptions } from '../../keyStore/IKeyStore';

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
   * Crypto factory defining the crypto API
   */
   private cryptoFactory: CryptoFactory;

  /**
   * Key store for storing keys
   */
   private keyStore: IKeyStore;

  /**
   * Authentication constructor
   * @param options Arguments to a constructor in a named object
   */
  constructor (options: DidProtocolOptions) {
    this.sender = options.sender;
    this.resolver = options.resolver;
    this.cryptoFactory = (<UserAgentOptions>this.sender.options).cryptoFactory;
    this.keyStore = (<UserAgentOptions>this.sender.options).keyStore;
  }

  /**
   * Unwrapping method for unwrapping requests/responses.
   * Decrypt a cipher using [PrivateKey] of Client Identifier and then verify payload.
   * @param keyReference key reference of private key in keystore used to decrypt.
   * @param cipher the cipher to be decrypted and verified by client Identifier.
   */
  public async decryptAndVerify(keyReference: string, cipher: Buffer): Promise<any> {
    
    // decrypt payload.
    const jws = await this.sender.decrypt(cipher, keyReference);
    
    // get identifier id from key id in header.
    const token : JwsToken = await JwsToken.deserialize(jws, <ISigningOptions> {
      cryptoFactory: this.cryptoFactory});
    const tokenHeaders = token.getHeader();
    const kid = tokenHeaders.get('kid').split('#');
    const id = kid[0];

    // create User Agent Options for Identifier
    const options = new UserAgentOptions();
    options.resolver = this.resolver;

    const identifier = new Identifier(id, options);
    const payload = await identifier.verify(jws);
    return JSON.parse(payload);
  }

  /**
   * Wrapping method for wrapping requests/responses.
   * Sign a payload using [PrivateKey] in client's keystore and encrypt payload using [PublicKey] 
   * @param payload the payload to be signed and encrypted by client Identifier.
   */
  public async signAndEncrypt(keyReference: string, payload: any, receiverId: string): Promise<string> {

    let stringifiedPayload: string;
    if (typeof(payload) === 'string') {
      stringifiedPayload = payload;
    } else {
      stringifiedPayload = JSON.stringify(payload);
    }
    
    const jws = await this.sender.sign(stringifiedPayload, keyReference);

    // create User Agent Options for Identifier
    const options = new UserAgentOptions();
    options.resolver = this.resolver;

    const receiverIdentifier = new Identifier(receiverId, options);
    return receiverIdentifier.encrypt(jws);
  }

}
