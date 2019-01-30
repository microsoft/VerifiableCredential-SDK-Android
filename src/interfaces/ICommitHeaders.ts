/**
 * Represents the headers of a commit returned by a Hub.
 */
export default interface ICommitHeaders {

  /** The Hub interface of the relevant object (e.g. Collections or Actions). */
  interface: string;

  /** The schema context of the relevant object. */
  context: string;

  /** The schema type of the relevant object. */
  type: string;

  /** The operation performed by this commit. */
  operation: 'create' | 'update' | 'delete';

  /** The time at which the commit was created. */
  committed_at: string;

  /** The commit strategy used by the relevant object. */
  commit_strategy: string;

  /** The subject (owner) of the relevant object. */
  sub: string;

  /** The fully-qualified Key ID of the key used to sign the commit. */
  kid: string;

  /** The issuer (creator) of the commit. */
  iss: string;

  /** The ID of the object to which this commit applies. */
  object_id: string;

  /** The revision hash (primary identifier) of this commit. */
  // rev: string;

  /** The application-specific metadata in this commit, if any. */
  meta?: any;

}
