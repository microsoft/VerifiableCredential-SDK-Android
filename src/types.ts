import HttpResolver from "./resolvers/HttpResolver";
import { PrivateKey } from "@decentralized-identity/did-auth-jose";

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
 * Type for defining a claim
 * TODO: coming up with a spec for this right now
 */
export interface ClaimObj {
  /**
   * the verified claim in the form of the jwt
   */
  jwt: string;

  /**
   * ui references
   */
  uiRef: any;

}

/**
 * Type for a Hub Session
 */
export interface HubSessionOptions {
  hubEndpoint: string;
  hubDid: string;
  resolver: HttpResolver;
  clientDid: string;
  clientPrivateKey: PrivateKey;
  targetDid: string;
}
