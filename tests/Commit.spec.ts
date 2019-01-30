import * as objectAssign from 'object-assign';
import Commit from '../src/Commit';
import ICommitSigner from '../src/interfaces/ICommitSigner';
import SignedCommit from '../src/SignedCommit';

const commitFields = {
  protected: {
    interface: 'Collections',
    context: 'schema.org',
    type: 'MusicPlaylist',
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

  describe('constructor', () => {
    it('should validate the given fields', async () => {

      ['protected', 'payload'].forEach((field) => {
        try {
          new Commit(objectAssign({}, commitFields, { [field]: null }));
          fail('Not expected to reach this point.');
        } catch (e) {
          // Expected
        }
      });

    });

  });

  describe('getProtectedHeaders()', () => {
    it('should return the protected headers', async () => {
      expect(commit.getProtectedHeaders()).toEqual(commitFields.protected);
    });
  });

  describe('getUnprotectedHeaders()', () => {
    it('should return the unprotected headers', async () => {
      expect(commit.getUnprotectedHeaders()).toEqual(commitFields.header);
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
