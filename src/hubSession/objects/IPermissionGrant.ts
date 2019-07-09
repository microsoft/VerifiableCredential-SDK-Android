/**
 * Context of the permission grant object
 */
export const PERMISSION_GRANT_CONTEXT = 'schema.identity.foundation/0.1';

/**
 * Type of the permission grant object
 */
export const PERMISSION_GRANT_TYPE = 'PermissionGrant';

/**
 * A Permission Grant object used to authorize access to certain schemas.
 */
export default interface IPermissionGrant {
  /**
   * DID of the object owner 
   */
  owner: string;
  /**
   * DID the permission was granted to
   */
  grantee: string;
  /**
   * Permission allowed following a POSIX-like string "CRUD" or "----"
   */
  allow: string;
  /**
   * Context of the object permission is being granted to
   */
  context: string;
  /**
   * Type of the object permission is being granted to
   */
  type: string;
}
