/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICommitProtector from './ICommitProtector';
import Commit from '../Commit';
import CryptoFactory from '../../crypto/plugin/CryptoFactory';
import { TSMap } from 'typescript-map';
import { ICryptoToken } from '../../crypto/protocols/ICryptoToken';
import IPayloadProtectionOptions from '../../crypto/protocols/IPayloadProtectionOptions';
import { IPayloadProtection } from '../../crypto/protocols/IPayloadProtection';
import JoseConstants from '../../crypto/protocols/jose/JoseConstants';
import ProtectionStrategy from '../../crypto/strategies/ProtectionStrategy';
import PayloadSigningStrategy from '../../crypto/strategies/PayloadSigningStrategy';
import { PublicKey } from '../..';
import UserAgentError from '../../UserAgentError';

export interface CommitProtectorOptions {

  /** 
   * The DID of the identity that will the commit. 
   */
  did: string;

  /** 
   * The private key reference to be used to sign the commit. 
   */
  signingKeyReference: string | undefined;

  /**
   * The public keys used for encrypting the payload
   */
  recipientsPublicKeys: PublicKey[] | undefined;

  /**
   * The protection protocol interface to use
   */
  payloadProtection: IPayloadProtection;

  /**
   * The payload protection options used to protect the commits
   */
  payloadProtectionOptions: IPayloadProtectionOptions;

  /**
   * Defines the protection strategy that will be used to protect the commit.
   */
  hubProtectionStrategy: ProtectionStrategy | undefined;
}

/**
 * Class which can apply a signature to a commit.
 */
export default class CommitProtector implements ICommitProtector {

  private did: string;
  private signingKeyReference: string | undefined;
  private payloadProtectionOptions: IPayloadProtectionOptions;
  private payloadProtection: IPayloadProtection;
  private hubProtectionStrategy: ProtectionStrategy | undefined;
  private recipientsPublicKeys: PublicKey[] | undefined;

  constructor(options: CommitProtectorOptions) {
    this.did = options.did;
    this.signingKeyReference = options.signingKeyReference;
    this.payloadProtectionOptions = options.payloadProtectionOptions;
    this.payloadProtection = options.payloadProtection;
    this.hubProtectionStrategy = options.hubProtectionStrategy;
    this.recipientsPublicKeys = options.recipientsPublicKeys;
  }

  /**
   * Protect the given commit.
   *
   * @param commit The commit to protect.
   */
  public async protect(commit: Commit): Promise<ICryptoToken> {
    let payload: string;
    if (typeof(commit.getPayload()) === 'string') {
      payload = commit.getPayload();
    } else {
      payload = JSON.stringify(commit.getPayload());
    }

    commit.validate();

    let protectedPayload: ICryptoToken = <ICryptoToken>{};

    if (!this.hubProtectionStrategy) {
      // Set a default protection stategy
      this.hubProtectionStrategy = new ProtectionStrategy();
      this.hubProtectionStrategy.payloadSigningStrategy = new PayloadSigningStrategy();
    }

    // Do signature
    if (this.hubProtectionStrategy.payloadSigningStrategy && this.hubProtectionStrategy.payloadSigningStrategy.enabled) {
      if (!this.signingKeyReference) {
        throw new UserAgentError(`The signing key reference is missing from the options`);
      }

      protectedPayload = await this.payloadProtection.sign(this.signingKeyReference, Buffer.from(payload), 'JwsCompactJson', this.payloadProtectionOptions);
    }

    // encrypt
    if (this.hubProtectionStrategy.PayloadEncryptionStrategy && this.hubProtectionStrategy.PayloadEncryptionStrategy.enabled) {
      if (!this.recipientsPublicKeys) {
        throw new UserAgentError(`The encryption public keys are missing from the options`);
      }

      protectedPayload = await this.payloadProtection.encrypt(this.recipientsPublicKeys, Buffer.from(payload), 'JweCompactJson', this.payloadProtectionOptions);
    }

  return <ICryptoToken>protectedPayload;    
  }

}
