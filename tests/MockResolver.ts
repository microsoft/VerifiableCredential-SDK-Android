import { IDidResolver, DidDocument } from '@decentralized-identity/did-common-typescript';
import { PublicKey } from '@decentralized-identity/did-auth-jose';

/**
 * Mock implementation of a DidResolver which will return the configured DID documents.
 */
export default class MockResolver implements IDidResolver {

  private keys: {[did: string]: PublicKey} = {};

  constructor(keys?: {[did: string]: PublicKey}) {
    if (keys) {
      Object.keys(keys).forEach(did => this.keys[did] = keys[did]);
    }
  }

  /**
   * Sets the key for a specific DID.
   */
  setKey(did: string, key: PublicKey) {
    this.keys[did] = key;
  }
  
  /**
   * Resolves the given DID.
   */
  async resolve(did: string) {

    const key = this.keys[did];

    if (!key) {
      throw new Error(`MockResolver has no entry for requested DID: ${did}`);
    }

    return {
      didDocument: new DidDocument({
        "@context": "https://w3id.org/did/v1",
        id: did,
        publicKey: [{
          id: key.kid,
          type: 'RsaVerificationKey2018',
          controller: did,
          publicKeyJwk: key
        }]
      })
    };
  }

}