/**
 * Interface representing a signed commit in Flattened JWS JSON Serialization.
 *
 * See: https://tools.ietf.org/html/rfc7515#section-7.2.1
 */
export default interface IFlattenedJws {

  /** The protected (signed) commit header. */
  protected: string;

  /** The unprotected (unverified) commit header. */
  header?: {[key: string]: any};

  /** The application-specific commit payload. */
  payload: string;

  /** The JWS signature. */
  signature: string;

}
