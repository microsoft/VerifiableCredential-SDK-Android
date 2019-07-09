import { IPermissionRequestPrompt } from "../requests/IRequestPrompt";

/**
 * Receipt for sending an OIDC response
 */
export default interface IResponseReceipt {
  /**
   * Array of credentials minted from this response
   */
  credentials: any[],
  /**
   * records for PermissionGrants created
   */
  permissionGrants: IPermissionReceipt[]
}

/**
 * Record of Identity Hub Permissions granted
 */
export interface IPermissionReceipt {
  /**
   * DID of the owner and permission granter
   */
  owner: string;
  /**
   * DID the permission was granted to
   */
  grantee: string;
  /**
   * Original definition of the permission request
   */
  definition: {
    /**
     * Name of the permission
     */
    name: string,
    /**
     * Description of the permission
     */
    description: string,
    /**
     * Icon to display with this permission
     */
    iconUrl?: string
  },
  /**
   * Object Ids of the permission grants
   */
  objects: string[]
}
