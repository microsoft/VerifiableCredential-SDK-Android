import RsaPrivateKey from '@decentralized-identity/did-auth-jose/dist/lib/crypto/rsa/RsaPrivateKey';
import { HttpResolver } from '@decentralized-identity/did-common-typescript';
import HubSession from './HubSession';
import HubWriteRequest from './requests/HubWriteRequest';
import CommitSigner from './crypto/CommitSigner';
import Commit from './Commit';
import HubObjectQueryRequest from './requests/HubObjectQueryRequest';
import HubCommitQueryRequest from './requests/HubCommitQueryRequest';
import CommitStrategyBasic from './CommitStrategyBasic';

// Fill these in with specific values.
const HTTP_RESOLVER = 'HTTP_RESOLVER_ENDPOINT_HERE';
const HUB_ENDPOINT = 'HUB_ENDPOINT_HERE';
const HUB_DID = 'HUB_DID_HERE';

// Fill in the DID to use
const DID = 'did:example:YOUR_DID_HERE';

/**
 * Fill in your full private key, including the `kid` field. The key must:
 * - Be an RSA private key in JWK format
 * - Match one of the public keys registered for your DID
 * - Include a "kid" field with the plain (not fully-qualified) key ID, e.g. "key-1"
 */
const PRIVATE_KEY = { kid:'key-1' };

async function runExample() {

  try {

    const kid = `${DID}#${PRIVATE_KEY.kid}`;
    const privateKey = RsaPrivateKey.wrapJwk(kid, PRIVATE_KEY);

    const session = new HubSession({
      hubEndpoint: HUB_ENDPOINT,
      hubDid: HUB_DID,
      resolver: new HttpResolver(HTTP_RESOLVER),
      clientDid: DID,
      clientPrivateKey: privateKey,
      targetDid: DID,
    });

    //
    // Write a new Commit to the Hub, creating a new object.
    //

    const commit = new Commit({
      protected: {
        committed_at: (new Date()).toISOString(),
        iss: DID,
        sub: DID,
        interface: 'Collections',
        context: 'http://schema.org',
        type: 'MusicPlaylist',
        operation: 'create',
        commit_strategy: 'basic',
      },
      payload: {
        title: 'My Playlist',
      },
    });

    const signer = new CommitSigner({
      did: DID,
      key: privateKey,
    });

    const signedCommit = await signer.sign(commit);

    const commitRequest = new HubWriteRequest(signedCommit);
    const commitResponse = await session.send(commitRequest);
    console.log(commitResponse);

    //
    // Read available objects from the Hub.
    //

    const queryRequest = new HubObjectQueryRequest({
      interface: 'Collections',
      context: 'http://schema.org',
      type: 'MusicPlaylist',
    });

    const queryResponse = await session.send(queryRequest);
    console.log(queryResponse);

    const objects = queryResponse.getObjects();

    //
    // Read the contents of a single object.
    //

    if (objects.length > 0) {
      const objectMetadata = objects[0];

      if (objectMetadata.commit_strategy !== 'basic') {
        throw new Error('Currently only the basic commit strategy is supported.');
      }

      const commitQueryRequest = new HubCommitQueryRequest({
        object_id: [objectMetadata.id],
      });

      const commitQueryResponse = await session.send(commitQueryRequest);
      const commits = commitQueryResponse.getCommits();

      const strategy = new CommitStrategyBasic();
      const objectState = await strategy.resolveObject(commits);

      console.log(objectState);
    }

  } catch (e) {
    console.error(e);
  }

}

runExample();
