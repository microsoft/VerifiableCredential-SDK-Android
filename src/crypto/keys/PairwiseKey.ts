/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import CryptoFactory from "../plugin/CryptoFactory";
import { CryptoAlgorithm } from "../keyStore/IKeyStore";
import KeyTypeFactory, { KeyType } from "./KeyTypeFactory";
import W3cCryptoApiConstants from "../utilities/W3cCryptoApiConstants";
import base64url from "base64url";
import EcPairwiseKey from "./ec/EcPairwiseKey";
import CryptoError from "../CryptoError";
import RsaPairwiseKey from "./rsa/RsaPairwiseKey";
import PrivateKey from "./PrivateKey";

/**
 * Class to model pairwise keys
 */
 export default class PairwiseKey {

  /**
   * Get or set the crypto factory to use, containing the crypto suite and the key store.
   */
   private cryptoFactory: CryptoFactory;
 
   // Set of master keys for the different persona's
   private masterKeys: Map<string, Buffer> = new Map<string, Buffer>();

  /**
   * Create an instance of @class PairwiseKey.
   * @param cryptoFactory The crypto factory object.
   */
   public constructor (cryptoFactory: CryptoFactory) {
     this.cryptoFactory = cryptoFactory;
   }

  /**
   * Generate a pairwise key for the specified algorithms
   * @param algorithm for the key
   * @param seedReference Reference to the seed
   * @param personaId Id for the persona
   * @param peerId Id for the peer
   */
   public async generatePairwiseKey(algorithm: EcKeyGenParams | RsaHashedKeyGenParams, seedReference: string, personaId: string, peerId: string): Promise<PrivateKey> {
    const personaMasterKey: Buffer = await this.generatePersonaMasterKey(seedReference, personaId);

    const keyType = KeyTypeFactory.create(algorithm);
    switch (keyType) {
      case KeyType.EC:
        return EcPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <EcKeyGenParams>algorithm, peerId);
      case KeyType.RSA:
        return RsaPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <RsaHashedKeyGenParams>algorithm, peerId);
    
      default:
        throw new CryptoError(algorithm, `Pairwise key for type '${keyType}' is not supported.`);
    }
  } 

  /**
   * Generate a pairwise master key.
   * @param seedReference  The master seed for generating pairwise keys
   * @param personaId  The owner DID
   */
   private async generatePersonaMasterKey (seedReference: string, personaId: string): Promise<Buffer> {
    let mk: Buffer | undefined = this.masterKeys.get(personaId);

    if (mk) {
      return mk;
    }

    // Get the seed
    const seed = <Buffer> await this.cryptoFactory.keyStore.get(seedReference);

    // Get the subtle crypto
    const crypto: SubtleCrypto = this.cryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac);

    // Generate the master key
    const alg: CryptoAlgorithm = { name: W3cCryptoApiConstants.Hmac, hash: W3cCryptoApiConstants.Sha512 };
    const jwk: JsonWebKey = {
      kty: 'oct',
      k: base64url.encode(seed)
    };

    const key = await crypto.importKey(W3cCryptoApiConstants.Jwk, jwk, alg, false, ['sign']);
    const masterKey = await crypto.sign(alg, key, Buffer.from(personaId));
    mk = Buffer.from(masterKey);
    this.masterKeys.set(personaId, mk); 
    return mk;
  }
}
