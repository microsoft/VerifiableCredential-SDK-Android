import OIDCAuthenticationRequest from "../../../crypto/protocols/did/requests/OIDCAuthenticationRequest";
import IPermissionGrant from "../../../hubSession/objects/IPermissionGrant";

/**
 * Contains all available resources for a given Open ID Connect Request
 */
export default interface IRequestPrompt extends OIDCAuthenticationRequest {
  /**
   * Hostname of the requester if available
   */
  host?: string,
  /**
   * Self asserted name of the requester
   */
  name?: string,
  /**
   * Requester Logo location
   */
  logoUrl?: URL,
  /**
   * Requester homepage location
   */
  homepage?: URL,
  /**
   * Requester data usage policy location
   */
  dataUsePolicy?: URL,
  /**
   * Requester terms of service location
   */
  termsOfService?: URL,
  /**
   * Credentials requested
   */
  credentialsRequested?: string[],
  /**
   * Credentials that will be generated from this request
   */
  credentialsMinted?: { // CARD INTERFACE THING NEEDED HERE (claim class)
    name: string,
    hexBackgroundColor: string
  }[],
  /**
   * Persistent access requests to Identity Hub objects
   */
  identityHubPermissionsRequested?: IPermissionRequestPrompt[]
}

export interface IPermissionRequestPrompt {
  /**
   * True if the permission is required
   */
  required: boolean;
  /**
   * Short name of the permission requested
   */
  name: string,
  /**
   * Description of what granting this permission means
   */
  description: string,
  /**
   * Icon to represent the objects or data requested in this permission
   */
  iconUrl?: URL
  /**
   * Permission Grant to be used
   */
  grants: IPermissionGrant[]
}
