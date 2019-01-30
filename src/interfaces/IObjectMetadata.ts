/**
 * Represents the metadata about an object returned by a Hub.
 */
export default interface IObjectMetadata {

  /** The Hub interface of the object (e.g. Collections or Actions). */
  interface: string;

  /** The schema context of the object. */
  context: string;

  /** The schema type of the object. */
  type: string;

  /** The ID of the object. */
  id: string;

  /** The fully-qualified DID of the entity which created the object. */
  created_by: string;

  /** The time at which the object was created. */
  created_at: string;

  /** The subject (owner) of the object. */
  sub: string;

  /** The commit strategy used by the object. */
  commit_strategy: string;

  /** The latest resolved state of the object's metadata, if any. */
  meta?: any;

}
