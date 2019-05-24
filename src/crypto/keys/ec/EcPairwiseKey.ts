/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import W3cCryptoApiConstants from "../../utilities/W3cCryptoApiConstants";
import base64url from "base64url";
import PrivateKey from "../PrivateKey";
import CryptoFactory from "../../plugin/CryptoFactory";
import { CryptoAlgorithm } from "../../keyStore/IKeyStore";
import { KeyType } from "../KeyTypeFactory";
import EcPrivateKey from "./EcPrivateKey";

// Create and initialize EC context
const BN = require('bn.js');
const elliptic = require('elliptic').ec;
const secp256k1 = new elliptic('secp256k1');

const SUPPORTED_CURVES = ['K-256', 'P-256K'];

/**
 * Class to model EC pairwise keys
 */
 export default class EcPairwiseKey {

  /**
   * Generate a pairwise key for the specified algorithms
   * @param cryptoFactory defining the key store and the used crypto api
   * @param personaMasterKey Master key for the current selected persona
   * @param algorithm for the key
   * @param peerId Id for the peer
   * @param extractable True if key is exportable
   */
  public static async generate(cryptoFactory: CryptoFactory, personaMasterKey: Buffer, algorithm: EcKeyGenParams, peerId: string): Promise<PrivateKey> {
    // This method is currently breaking the subtle crypto pattern and needs to be fixed to be platform independent
    // Get the subtle crypto
    const crypto: SubtleCrypto = cryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac);

    // Generate the master key
    const alg: CryptoAlgorithm = { name: W3cCryptoApiConstants.Hmac, hash: W3cCryptoApiConstants.Sha256 };
    const signingKey: JsonWebKey = {
      kty: 'oct',
      k: base64url.encode(personaMasterKey)
    };

    const key = await crypto.importKey(W3cCryptoApiConstants.Jwk, signingKey, alg, false, ['sign']);
    const pairwiseKeySeed = await crypto.sign(alg, key, Buffer.from(peerId));
 
    if (SUPPORTED_CURVES.indexOf(algorithm.namedCurve) === -1) {
      throw new Error(`Curve ${algorithm.namedCurve} is not supported`);
    }

    const privateKey = new BN(Buffer.from(pairwiseKeySeed));
    const pair = secp256k1.keyPair({ priv: privateKey });
    const pubKey = pair.getPublic();
    const d = privateKey.toArrayLike(Buffer, 'be', 32);
    const x = pubKey.x.toArrayLike(Buffer, 'be', 32);
    const y = pubKey.y.toArrayLike(Buffer, 'be', 32);
    return <EcPrivateKey>{
      crv: algorithm.namedCurve,
      d: base64url.encode(d),
      x: base64url.encode(x),
      y: base64url.encode(y),
      kty:KeyType.EC
    };
  } 
}
