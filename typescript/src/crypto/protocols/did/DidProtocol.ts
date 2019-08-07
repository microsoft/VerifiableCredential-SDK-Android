/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import Identifier from '../../../Identifier';
 import IResolver from '../../../resolvers/IResolver';
 import JwsToken from '../jose/jws/JwsToken';
 import CryptoFactory from '../../plugin/CryptoFactory';
 import UserAgentOptions from '../../../UserAgentOptions';
 import IKeyStore from '../../keyStore/IKeyStore';
 import { IJwsSigningOptions } from '../jose/IJoseOptions';
 import { PublicKey } from '../../..';
 import { TSMap } from 'typescript-map';
 import JoseConstants from '../jose/JoseConstants';
 import CryptoProtocolError from '../CryptoProtocolError';
 
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
     * User agent options
     */
    private options: UserAgentOptions;
 
   /**
    * Authentication constructor
    * @param options Arguments to a constructor in a named object
    */
   constructor (options: DidProtocolOptions) {
     this.sender = options.sender;
     this.resolver = options.resolver;
     this.options = <UserAgentOptions>this.sender.options;
     this.cryptoFactory = this.options.cryptoFactory;
     this.keyStore = this.options.keyStore;
   }
 
   /**
    * Unwrapping method for unwrapping requests/responses.
    * Decrypt a cipher using [PrivateKey] of Client Identifier and then verify payload.
    * @param encryptionKeyReference key reference of private key in keystore used to decrypt.
    * @param cipher the cipher to be decrypted and verified by client Identifier.
    */
   public async decryptAndVerify(encryptionKeyReference: string, cipher: Buffer): Promise<any> {
     
     // decrypt payload.
     const jws = await this.sender.decrypt(cipher, encryptionKeyReference);
     
     // get identifier id from key id in header.
     const options = <IJwsSigningOptions> {
       cryptoFactory: this.cryptoFactory};
     const token : JwsToken = await JwsToken.deserialize(jws, options);
     let kid = undefined;
     if (token.signatures[0].protected && token.signatures[0].protected.get(JoseConstants.Kid)) {
       kid = token.signatures[0].protected.get(JoseConstants.Kid);
     } else if (token.signatures[0].header && token.signatures[0].header.get(JoseConstants.Kid)) {
       kid = token.signatures[0].header.get(JoseConstants.Kid);
     }
 
     if (!kid) {
       throw new CryptoProtocolError('DidProtocol', 'The jws token does not contain a kid in protected or header');
     }
 
     const id = kid.split('#')[0];
 
     const identifier = new Identifier(id, this.options);
     let payload = await identifier.verify(jws);
     return JSON.parse(payload);
    }
 
   /**
    * Wrapping method for wrapping requests/responses.
    * Sign a payload using [PrivateKey] in client's keystore and encrypt payload using [PublicKey] 
    * @param payload the payload to be signed and encrypted by client Identifier.
    * #param receiverId identifier for the receiver.
    */
   public async signAndEncrypt(payload: any, receiverId: string): Promise<string> {
 
     let stringifiedPayload: string;
     if (typeof(payload) === 'string') {
       stringifiedPayload = payload;
     } else {
       stringifiedPayload = JSON.stringify(payload);
     }
     
     const jws = await this.sender.sign(stringifiedPayload, <string>(<UserAgentOptions>this.sender.options).cryptoOptions.signingKeyReference);
 
     // create User Agent Options for Identifier
 
     const receiverIdentifier = new Identifier(receiverId, this.options);
     return receiverIdentifier.encrypt(jws);
   }
 
 }
