import ICommitSigner from './ICommitSigner';
import Commit from '../Commit';
import SignedCommit from '../SignedCommit';
import IKeyStore from '../../crypto/keyStore/IKeyStore';
import CryptoFactory from '../../crypto/plugin/CryptoFactory';
import CryptoOperations from '../../crypto/plugin/CryptoOperations';
import JwsToken from '../../crypto/protocols/jws/JwsToken';
import { ProtectionFormat } from '../../crypto/keyStore/ProtectionFormat';
import objectAssign from 'object-assign';
import { IJwsSigningOptions } from '../../crypto/protocols/jose/IJoseOptions';

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

    commit.validate();

    const protectedHeaders = commit.getProtectedHeaders();
    const finalProtectedHeaders: any = objectAssign({}, protectedHeaders, {
      iss: this.did,
    });

    // const jws = new JwsToken(commit.getPayload(), new CryptoFactory([this.cryptoSuite]));
    // const signed = await jws.sign(key, <any> finalProtectedHeaders); // Need to broaden TypeScript definition of JwsToken.sign().
    const signingOptions: IJwsSigningOptions = {cryptoFactory: this.cryptoFactory, protected: finalProtectedHeaders};
    const jws = new JwsToken(signingOptions);
    const signed = await jws.sign(this.keyRef, commit.getPayload(), ProtectionFormat.JwsCompactJson);
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
