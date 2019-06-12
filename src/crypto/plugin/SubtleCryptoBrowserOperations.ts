/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import CryptoOperations from './CryptoOperations';
 import { SubtleCrypto } from 'webcrypto-core';
 import { SubtleCryptoElliptic } from '@microsoft/useragent-plugin-secp256k1';
 // import nodeWebcryptoOssl from 'node-webcrypto-ossl';
 
 /**
  * Default crypto suite implementing the default plugable crypto layer
  *  */
 export default class SubtleCryptoBrowserOperations extends CryptoOperations {
   private defaultCrypto: SubtleCrypto | undefined;
   public crypto: any;
 
   constructor () {
     super();
    }
 
  /**
   * Gets all of the key encryption Algorithms from the plugin
   * @returns a subtle crypto object for key encryption/decryption
   */
  public getKeyEncrypters (): SubtleCrypto {
   return this.getSubtleCrypto();
 }
 
  /**
   * Gets all of the key sharing encryption Algorithms from the plugin
   * @returns a subtle crypto object for key sharing encryption/decryption
   */
  public getSharedKeyEncrypters (): SubtleCrypto {
   return this.getSubtleCrypto();
 }
 
  /**
    * Get all of the symmetric encrypter algorithms from the plugin
   * @returns a subtle crypto object for symmetric encryption/decryption
    */
   public getSymmetricEncrypters (): SubtleCrypto {
     return this.getSubtleCrypto();
   }
 
  /**
   * Gets all of the message signing Algorithms from the plugin
  * @returns a subtle crypto object for message signing
    */
   public getMessageSigners (): SubtleCrypto {
     return <any> new SubtleCryptoElliptic();
   }
 
  /**
   * Gets all of the MAC signing Algorithms from the plugin. 
   * Will be used for primitive operations such as key generation.
  * @returns a subtle crypto object for message signing
    */
   public messageAuthenticationCodeSigners (): SubtleCrypto {
     return this.getSubtleCrypto();
   }
 
  /**
   * Gets all of the message digest Algorithms from the plugin. 
  * @returns a subtle crypto object for message digests
    */
   public getMessageDigests (): SubtleCrypto {
     return this.getSubtleCrypto();
   }
 
   /**
    * Returns the @class SubtleCrypto ipmplementation for the current environment
    */
   public getSubtleCrypto(): SubtleCrypto {
   // tslint:disable-next-line:no-typeof-undefined
   if (typeof window !== 'undefined') {
     // return browser api
     return <SubtleCrypto>window.crypto.subtle;
     } else {
       // return nodejs api
       if (this.defaultCrypto) {
         return this.defaultCrypto;
       }
       this.defaultCrypto = <SubtleCrypto>this.crypto.subtle;
       return this.defaultCrypto;
     }
    }
 }
