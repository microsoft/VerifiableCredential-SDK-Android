import HubRequest from './HubRequest';
import SignedCommit from '../SignedCommit';

/**
 * Represents a request to commit the given Commit object to an Identity Hub.
 */
export default class HubCommitWriteRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isWriteRequest = true;

  constructor(commit: SignedCommit) {
    super('WriteRequest', {
      commit: commit.toFlattenedJson(),
    });
  }

}
