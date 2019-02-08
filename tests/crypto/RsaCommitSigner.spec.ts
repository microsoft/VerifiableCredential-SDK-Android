import { ICommitProtectedHeaders } from '@decentralized-identity/hub-common-js';
import RsaCommitSigner from '../../src/crypto/RsaCommitSigner';
import RsaPrivateKey from '@decentralized-identity/did-auth-jose/dist/lib/crypto/rsa/RsaPrivateKey';
import Commit from '../../src/Commit';

describe('RsaCommitSigner', () => {

  describe('sign()', () => {

    it('should sign a commit', async () => {
      const testDid = 'did:example:person.id';
      const testKid = `${testDid}#key-1`;
      const testKey = await RsaPrivateKey.generatePrivateKey(testKid);

      const protectedHeaders: Partial<ICommitProtectedHeaders> = {
        interface: 'Collections',
        context: 'schema.org',
        type: 'MusicPlaylist',
        operation: 'create',
        committed_at: '2019-01-01',
        commit_strategy: 'basic',
        sub: 'did:example:sub.id',
        // iss and kid left out intentionally
      };

      const payload = {
        name: "Test"
      };

      const commit = new Commit({
        protected: protectedHeaders,
        payload
      });

      const signer = new RsaCommitSigner({
        did: testDid,
        key: testKey
      });

      const signedCommit = await signer.sign(commit);

      expect(signedCommit.getPayload()).toEqual(payload);

      const signedProtectedHeaders = signedCommit.getProtectedHeaders();
      Object.keys(protectedHeaders).forEach((headerKey) => {
        expect((signedProtectedHeaders as any)[headerKey]).toEqual((protectedHeaders as any)[headerKey]);
      })

      expect(signedProtectedHeaders.iss).toEqual(testDid);
      expect(signedProtectedHeaders.kid).toEqual(testKid);
    });

    it('should throw an error if a commit is not valid', async () => {
      const testDid = 'did:example:person.id';
      const testKid = `${testDid}#key-1`;
      const testKey = await RsaPrivateKey.generatePrivateKey(testKid);

      const commit = new Commit({
        protected: {
          interface: 'Collections',
          context: 'schema.org',
          // type: 'MusicPlaylist', // left out intentionally
          operation: 'create',
          committed_at: '2019-01-01',
          commit_strategy: 'basic',
          sub: 'did:example:sub.id',
        },
        payload: {
          name: "Test"
        }
      });

      const signer = new RsaCommitSigner({
        did: testDid,
        key: testKey
      });

      try {
        await signer.sign(commit);
        fail('Not expected to reach this point.');
      } catch (err) {
        expect(err.message).toContain("Commit 'protected.type' field must be");
      }
    });

  });
});
