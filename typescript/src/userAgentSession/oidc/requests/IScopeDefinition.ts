/**
 * OIDC Scope Definition format
 */
export default interface IScopeDefinition {
  /**
   * OIDC definition url where this is hosted
   */
  value: string;
  /**
   * Description of the scope of permissions being requested
   */
  resourceBundle: {
    /**
     * Name of the permission
     */
    name: string;
    /**
     * Description of the permission
     */
    description: string;
    /**
     * Icon to display with this permission
     */
    icon_uri: string
  },
  /**
   * Identity Hub permission requests to ask for
   */
  access: {
    /**
     * Object type to ask permission for
     */
    resource_type: string,
    /**
     * Object access requested
     */
    allow: string,
  }[]
}

/**
 * Scope request format following OIDC Claims request format
 */
export interface IScopeRequest {
  [url: string]: null | {essential?: boolean, value?: string, values?: string[]},
}
