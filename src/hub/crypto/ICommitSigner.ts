import Commit from '../Commit';
import SignedCommit from '../SignedCommit';

/**
 * Interface representing an object which can sign a commit.
 */
export default interface ICommitSigner {

  /**
   * Signs the given commit.
   *
   * @param commit The commit to sign.
   */
  sign(commit: Commit): Promise<SignedCommit>;

}
