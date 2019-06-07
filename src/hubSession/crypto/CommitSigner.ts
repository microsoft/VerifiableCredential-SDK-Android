import ICommitSigner from './ICommitSigner';
import Commit from '../Commit';
import SignedCommit from '../SignedCommit';
import IKeyStore, { ISigningOptions } from '../../crypto/keyStore/IKeyStore';
import CryptoFactory from '../../crypto/plugin/CryptoFactory';
import CryptoOperations from '../../crypto/plugin/CryptoOperations';
import JwsToken from '../../crypto/protocols/jws/JwsToken';
import { ProtectionFormat } from '../../crypto/keyStore/ProtectionFormat';
import objectAssign from 'object-assign';
import { TSMap } from 'typescript-map';

interface CommitSignerOptions {

  /** 
   * The DID of the identity that will the commit. 
   */
  did: string;

  /** 
   * The private key reference to be used to sign the commit. 
   */
  keyReference: string;

  /**
   * KeyStore that holds the private key to be used to sign the commit.
   */
  keyStore: IKeyStore;

  /** 
   * The CryptoSuite to be used to for the algorithm to use to sign the commit
   */
  suite?: CryptoOperations;

}

/**
 * Class which can apply a signature to a commit.
 */
export default class CommitSigner implements ICommitSigner {

  private did: string;
  private keyRef: string;
  private cryptoFactory: CryptoFactory;

  constructor(options: CommitSignerOptions) {
    this.did = options.did;
    this.keyRef = options.keyReference;
    this.cryptoFactory = new CryptoFactory(options.keyStore, options.suite);
  }

  /**
   * Signs the given commit.
   *
   * @param commit The commit to sign.
   */
  public async sign(commit: Commit): Promise<SignedCommit> {

    let payload: string;
    if (typeof(commit.getPayload()) === 'string') {
      payload = commit.getPayload();
    } else {
      payload = JSON.stringify(commit.getPayload());
    }

    commit.validate();

    const protectedHeaders = commit.getProtectedHeaders();
    const finalProtectedHeaders = new TSMap<string, string>([
                                    ['iss', this.did],
                                    ['commit_strategy', <string> protectedHeaders.commit_strategy],
                                    ['commited_at', <string> protectedHeaders.committed_at],
                                    ['context', <string> protectedHeaders.context],
                                    ['interface', <string> protectedHeaders.interface],
                                    ['operation', <string> protectedHeaders.operation],
                                    ['sub', <string> protectedHeaders.sub],
                                    ['type', <string> protectedHeaders.type]
                                  ]);

    // const jws = new JwsToken(commit.getPayload(), new CryptoFactory([this.cryptoSuite]));
    // const signed = await jws.sign(key, <any> finalProtectedHeaders); // Need to broaden TypeScript definition of JwsToken.sign().
    const signingOptions: ISigningOptions = {cryptoFactory: this.cryptoFactory, 
                                             protected: finalProtectedHeaders,
                                             header: new TSMap<string, string>([
                                              ['alg', ''],
                                              ['kid', '']
                                          ])};
    const jws = new JwsToken(signingOptions);
    const signed = await jws.sign(this.keyRef, Buffer.from(payload), ProtectionFormat.JwsCompactJson);
    const serializedCompactJws = signed.serialize();
    const [outputHeaders, outputPayload, outputSignature] = serializedCompactJws.split('.');

    return new SignedCommit({
      protected: outputHeaders,
      payload: outputPayload,
      header: commit.getUnprotectedHeaders(),
      signature: outputSignature,
    });
  }

}
