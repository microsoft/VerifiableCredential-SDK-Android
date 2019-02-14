import SignedCommit from './SignedCommit';

/**
 * Resolves the final state of an object from the constituent set of commits for that object.
 *
 * This class works only with objects using the `basic` commit strategy.
 */
export default class CommitStrategyBasic {

  /**
   * Resolves the current state of an object with the `basic` commit strategy.
   *
   * TODO: This class currently returns only the raw object payload. Once we add an object instance
   * class to the SDK (e.g. `HubObject`), this method will no longer be called directly, and will
   * also need to return the app-readable object metadata.
   *
   * Currently returns `null` if the object was deleted, otherwise returns the most recent payload.
   *
   * @param commits The entire known set of commits for the object.
   */
  async resolveObject(commits: SignedCommit[]) {

    if (!commits || commits.length === 0) {
      return null;
    }

    // tslint:disable:align
    const currentState = commits.reduce((latest, candidate) => {
      return this.compareCommits(latest, candidate) < 0
        ? candidate
        : latest;
    }, commits[0]);
    // tslint:enable:align

    return currentState.getProtectedHeaders().operation === 'delete'
      ? null
      : currentState.getPayload();
  }

  /**
   * Compares two commits (which must belong to the same object) in order to evaulate which one is
   * more recent.
   *
   * Follows the conventions of the JavaScript sort() method:
   *  - `-1` indicates that a comes before (i.e. is older than b)
   *  - `1` indicates that a comes after (i.e. is newer than b)
   *
   * @param a The first commit to compare.
   * @param b The second commit to compare.
   */
  protected compareCommits(a: SignedCommit, b: SignedCommit) {
    if (a.getObjectId() !== b.getObjectId()) {
      throw new Error('Cannot compare commits from different objects.');
    }

    const aHeaders = a.getProtectedHeaders();
    const bHeaders = b.getProtectedHeaders();

    if (aHeaders.operation === 'create') return -1;
    if (bHeaders.operation === 'create') return 1;

    if (aHeaders.operation === 'delete') return 1;
    if (bHeaders.operation === 'delete') return -1;

    const aDate = Date.parse(aHeaders.committed_at);
    const bDate = Date.parse(bHeaders.committed_at);

    if (aDate !== bDate) {
      return aDate < bDate ? -1 : 1;
    }

    return a.getRevision() < b.getRevision() ? -1 : 1;
  }

}
