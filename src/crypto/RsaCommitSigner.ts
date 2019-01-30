import { PrivateKey, JwsToken, CryptoFactory, RsaCryptoSuite } from '@decentralized-identity/did-auth-jose';
import ICommitSigner from '../interfaces/ICommitSigner';
import Commit from '../Commit';
import SignedCommit from '../SignedCommit';

interface RsaCommitSignerOptions {

  /** The DID of the identity that will the commit. */
  did: string;

  /** The private key to be used to sign the commit. */
  key: PrivateKey;

}

/**
 * Class which can apply a RSA signature to a commit.
 */
export default class RsaCommitSigner implements ICommitSigner {

  private did: string;
  private key: PrivateKey;

  constructor(options: RsaCommitSignerOptions) {
    this.did = options.did;
    this.key = options.key;
  }

  /**
   * Signs the given commit.
   *
   * @param commit The commit to sign.
   */
  public async sign(commit: Commit): Promise<SignedCommit> {

    const protectedHeaders = commit.getProtectedHeaders();
    protectedHeaders['iss'] = this.did;

    const jws = new JwsToken(commit.getPayload(), new CryptoFactory([new RsaCryptoSuite()]));
    const signed = await jws.sign(this.key, protectedHeaders as any); // TODO: Need to broaden TypeScript definition of JwsToken.sign().

    const [outputHeaders, outputPayload, outputSignature] = signed.split('.');

    return new SignedCommit({
      protected: outputHeaders,
      payload: outputPayload,
      header: commit.getUnprotectedHeaders(),
      signature: outputSignature,
    });
  }

}
