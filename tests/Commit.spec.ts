import { alter, explain } from './TestUtils';
import Commit, { ICommitFields } from '../src/Commit';
import ICommitSigner from './crypto/ICommitSigner';
import SignedCommit from '../src/SignedCommit';

const commitFields: ICommitFields = {
  protected: {
    interface: 'Collections',
    context: 'schema.org',
    type: 'MusicPlaylist',
    operation: 'create',
    committed_at: '2019-01-01',
    commit_strategy: 'basic',
    sub: 'did:example:sub.id'
  },
  payload: {
    title: 'My Playlist',
  },
  header: {
    rev: 'abc',
  },
};

const commit = new Commit(commitFields);

describe('Commit', () => {

  describe('validate', () => {

    const invalidStrings = ['', null, undefined, true, false, 7, [], [''], {}];

    const invalidCases: {[field: string]: any[]} = {
      'protected.interface': invalidStrings,
      'protected.context': invalidStrings,
      'protected.type': invalidStrings,
      'protected.committed_at': invalidStrings,
      'protected.commit_strategy': invalidStrings,
      'protected.sub': invalidStrings,
      'protected.operation': [...invalidStrings, 'other'],
      'protected': [true, false, null, undefined, {}, [], 77],
      'payload': [true, false, null, undefined, 77],
    };

    Object.keys(invalidCases).forEach((field) => {
      const cases = invalidCases[field];
      cases.forEach(invalidValue => {
        it(`should reject '${field}' set to ${explain(invalidValue)}`, async () => {
          const alteredFields = alter(commitFields, { [field]: invalidValue });
          const commit = new Commit(alteredFields);
          expect(commit.isValid()).toBeFalsy();
        });
      });
    });

    it('should ensure object_id is not set for a create commit', async () => {

      const alteredFields = alter(commitFields, {
        'protected.operation': 'create',
        'protected.object_id': 'abc'
      });

      const commit = new Commit(alteredFields);
      expect(commit.isValid()).toBeFalsy();

    });

    it('should ensure object_id is set for an update/delete commit', async () => {

      ['update', 'delete'].forEach((operation) => {
        const alteredFields = alter(commitFields, {
          'protected.operation': operation,
          'protected.object_id': undefined
        });
        const commit = new Commit(alteredFields);
        expect(commit.isValid()).toBeFalsy();
      });

    });

    it('should validate a correct commit', async () => {
        const commit = new Commit(commitFields);
        expect(commit.isValid()).toBeTruthy();
    });

  });

  describe('getProtectedHeaders()', () => {
    it('should return the protected headers', async () => {
      expect(commit.getProtectedHeaders()).toEqual(commitFields.protected);
    });
  });

  describe('getUnprotectedHeaders()', () => {
    it('should return the unprotected headers', async () => {
      expect(commit.getUnprotectedHeaders()).toEqual(commitFields.header as any);
    });
  });

  describe('getPayload()', () => {
    it('should return the payload', async () => {
      expect(commit.getPayload()).toEqual(commitFields.payload);
    });
  });

  describe('sign()', () => {
    it('should call sign() on the given signer', async () => {

      const signedCommit = new SignedCommit({
        protected: '',
        payload: '',
        signature: '',
      });

      const signer: ICommitSigner = {
        sign: async (): Promise<SignedCommit> => {
          return signedCommit;
        },
      };

      spyOn(signer, 'sign').and.callThrough();
      const returnValue = await commit.sign(signer as any);

      expect(signer.sign).toHaveBeenCalled();
      expect(returnValue).toEqual(signedCommit);
    });
  });

});
