
/**
 * Manifest file used to describe the OIDC requester
 * @see https://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata
 */
export default interface IManifest {
  /** 
   * Context of the manifest file
   */
  '@context'?: string,
  /**
   * Type of the manifest file
   */
  '@type'?: string | string[],
  /**
   * Requester name
   */
  client_name?: string,
  /**
   * Requester logo location
   */
  logo_uri?: string,
  /**
   * Requester homepage
   */
  client_uri?: string,
  /**
   * Requester data use policy location
   */
  policy_uri?: string,
  /**
   * Requester Terms of Service location
   */
  tos_uri?: string,
}
