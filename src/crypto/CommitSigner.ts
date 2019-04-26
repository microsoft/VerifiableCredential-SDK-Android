import { PrivateKey, JwsToken, CryptoFactory, RsaCryptoSuite, CryptoSuite } from '@decentralized-identity/did-auth-jose';
import ICommitSigner from './ICommitSigner';
import Commit from '../Commit';
import SignedCommit from '../SignedCommit';
import objectAssign = require('object-assign');

interface CommitSignerOptions {

  /** The DID of the identity that will the commit. */
  did: string;

  /** The private key to be used to sign the commit. */
  key: PrivateKey;

  /** The CryptoSuite to be used to for the algorithm to use to sign the commit */
  cryptoSuite?: CryptoSuite;

}

/**
 * Class which can apply a signature to a commit.
 */
export default class CommitSigner implements ICommitSigner {

  private did: string;
  private key: PrivateKey;
  private cryptoSuite: CryptoSuite;

  constructor(options: CommitSignerOptions) {
    this.did = options.did;
    this.key = options.key;
    if (!options.cryptoSuite) {
      this.cryptoSuite = new RsaCryptoSuite();
    } else {
      this.cryptoSuite = options.cryptoSuite;
    }
  }

  /**
   * Signs the given commit.
   *
   * @param commit The commit to sign.
   */
  public async sign(commit: Commit): Promise<SignedCommit> {

    commit.validate();

    const protectedHeaders = commit.getProtectedHeaders();
    const finalProtectedHeaders = objectAssign({}, protectedHeaders, {
      iss: this.did,
    });

    const jws = new JwsToken(commit.getPayload(), new CryptoFactory([this.cryptoSuite]));
    const signed = await jws.sign(this.key, finalProtectedHeaders as any); // TODO: Need to broaden TypeScript definition of JwsToken.sign().

    const [outputHeaders, outputPayload, outputSignature] = signed.split('.');

    return new SignedCommit({
      protected: outputHeaders,
      payload: outputPayload,
      header: commit.getUnprotectedHeaders(),
      signature: outputSignature,
    });
  }

}
