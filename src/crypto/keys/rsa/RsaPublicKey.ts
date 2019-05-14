import PublicKey from '../PublicKey';
import { KeyType } from '../KeyType';

/**
 * Represents an RSA public key
 * @class
 * @extends PublicKey
 */
export default class RsaPublicKey extends PublicKey {
  /** 
   * Public exponent 
   */
  public e: string | undefined;
  /** 
   * Modulus 
   */
  public n: string | undefined;
  /**
   * Set the EC key type
   */
  kty = KeyType.RSA;
  /**
   * Set the default algorithm
   */
  alg = 'RS256';
}
