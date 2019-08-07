import Commit from '../Commit';
import { ICryptoToken } from '../../crypto/protocols/ICryptoToken';

/**
 * Interface representing an object which can sign a commit.
 */
export default interface ICommitProtector {

  /**
   * Protect the given commit.
   *
   * @param commit The commit to protect.
   */
  protect(commit: Commit): Promise<ICryptoToken>;

}
