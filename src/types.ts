/**
 * Type for defining a identifier document
 * public key.
 */
export interface PublicKey {
  /**
   * The id of the public key in the format
   * #{keyIdentifier}.
   */
  id: string;

  /**
   * The type of the public key.
   */
  type: string;

  /**
   * The onwer of the key.
   */
  owner?: string;

  /**
   * The JWK public key.
   */
  publicKeyJwk: any;
}

/**
 * Type for defining a identifier document
 * authentication reference.
 */
export interface AuthenticationReference {
  /**
   * The type of the authentication reference.
   */
  type: string;

  /**
   * A public key reference in the format
   * #{keyIdentifier}.
   */
  publicKeyReference: string;
}

/**
 * Type for defining a identifier document
 * service reference.
 */
export interface ServiceReference {
  /**
   * The type of the service reference.
   */
  type: string;

  /**
   * A public key reference in the format
   * #{keyIdentifier}.
   */
  publicKeyReference: string;

  /**
   * The service endpoint for the
   * service reference
   */
  serviceEndpoint: ServiceEndpoint;
}

/**
 * Type for defining a identifier document
 * service reference endpoint.
 */
export interface ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  context: string;

  /**
   * The type of the service reference.
   */
  type: string;
}

/**
 * Type for defining a identifier document
 * service reference endpoint for a host.
 */
export interface HostServiceEndpoint extends ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  locations: Array<string>;
}

/**
 * Type for defining a identifier document
 * service reference endpoint for a user.
 */
export interface UserServiceEndpoint extends ServiceEndpoint {
  /**
   * The type of the service reference.
   */
  instances: Array<string>;
}

/**
 * Type for defining a URI as outlined in the claim spec.
 */
export interface UriDescription {
  /**
   * the type of URI.
   */
  '@type': string;

  /**
   * the actual URI.
   */
  uri: string;

  /**
   * the description of the URI.
   */
  description: string;
}

/**
 * Type for data inputs that are allowed on a ClaimManifest.
 */
export interface DataInput {
  /**
   * type of input
   */
  type: 'data';

  /**
   * a variety of proofs/prerequisites that can be accepted.
   * e.g. [A]
   */
  group: Array<string>;

  /**
   * what data input is used for.
   * e.g. routing_number
   */
  field: string;

  /**
   * contraints on the input
   * e.g. maxLength: 9
   */
  value: any;
}

/**
 * Type for claim inputs that are allowed on a ClaimManifest
 */
export interface CredentialInput {
  /**
   * type of input
   */
  type: 'credential';

  /**
   * a variety of proofs/prerequisites that can be accepted.
   * e.g. [A]
   */
  group: Array<string>;

  /**
   * Schema used for the specific credential.
   */
  schema: string;

  /**
   * constraints on the credential
   * e.g. what issuers will be accepted.
   */
  constraints: {
    issuers: Array<string>
  };
}
