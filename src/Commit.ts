import ICommitSigner from './interfaces/ICommitSigner';
import ICommitHeaders from './interfaces/ICommitHeaders';
import { SignedCommit } from './index';

/**
 * Fields that can be specified when creating a new commit.
 */
export interface ICommitFields {

  /** Fields to include in the protected (signed) commit header. */
  protected: Partial<ICommitHeaders>;

  /** Fields to include in the unprotected (unverified) commit header. */
  header?: {[key: string]: any};

  /** The application-specific commit payload. */
  payload: object | string;

}

/**
 * Represents a new (i.e pending, unsigned) commit which will create, update, or delete an object in
 * a user's Identity Hub.
 */
export default class Commit {

  private fields: ICommitFields;

  constructor(fields: ICommitFields) {
    this.validate(fields);
    this.fields = fields;
  }

  /**
   * Checks the given fields for validity.
   *
   * @param fields The fields to check.
   */
  private validate(fields: ICommitFields) {
    if (!fields.protected) {
      throw new Error("Commit must specify the 'protected' field.");
    }

    if (!fields.payload) {
      throw new Error("Commit must specify the 'payload' field.");
    }
  }

  /**
   * Returns the headers which will be signed/encrypted.
   */
  getProtectedHeaders(): Partial<ICommitHeaders> {
    return this.fields.protected;
  }

  /**
   * Returns the (optional) headers which will not be signed/encrypted.
   */
  getUnprotectedHeaders(): {[key: string]: any} {
    return this.fields.header || {};
  }

  /**
   * Returns the application-specific payload for this commit.
   */
  getPayload(): any {
    return this.fields.payload;
  }

  /**
   * Returns a copy of this commit signed with the given signer.
   *
   * @param signer The signer to use to sign the commit.
   */
  sign(signer: ICommitSigner): Promise<SignedCommit> {
    return signer.sign(this);
  }

}
