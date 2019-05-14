import PublicKey from '../PublicKey';
import { KeyType } from '../KeyType';

/**
 * Represents an Elliptic Curve public key
 * @class
 * @extends PublicKey
 */
export default class EcPublicKey extends PublicKey {
  /** 
   * curve 
   */
  public crv: string | undefined;
  /** 
   * x co-ordinate 
   */
  public x: string | undefined;
  /** 
   * y co-ordinate 
   */
  public y: string | undefined;
  /**
   * Set the EC key type
   */
  kty = KeyType.EC;
}
