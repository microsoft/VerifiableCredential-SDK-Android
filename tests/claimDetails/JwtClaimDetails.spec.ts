/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import JwtClaimDetails from '../../src/claimDetails/JwtClaimDetails';
import { PrivateKeyRsa, JwsToken, CryptoFactory, RsaCryptoSuite } from '@decentralized-identity/did-auth-jose';

/**
 * Tests the JwtClaimDetails Class.
 */
describe('JwtClaimDetails', () => {

  it('should expect create to have been called.', () => {
    spyOn(JwtClaimDetails, 'create').and.callThrough();
    const payload = {test: 'test'};
    const claimDetails = JwtClaimDetails.create(payload);
    expect(JwtClaimDetails.create).toHaveBeenCalled();
    expect(claimDetails).toBeDefined();
  });

  it('should expect sign to not have been called.', () => {
    const payload = {test: 'test'};
    const claimDetails = JwtClaimDetails.create(payload);
    spyOn(claimDetails, 'sign');
    expect(claimDetails.sign).not.toHaveBeenCalled();
  });

  it('should expect sign to have been called.', async done => {
    const payload = {test: 'test'};
    const claimDetails = JwtClaimDetails.create(payload);
    spyOn(claimDetails, 'sign').and.callThrough();
    const rsaPrivateKey = await PrivateKeyRsa.generatePrivateKey('test');
    const jws = await claimDetails.sign(rsaPrivateKey);
    expect(claimDetails.sign).toHaveBeenCalled();
    expect(jws).toBeDefined();
    done();
  });

  it('should expect verify to not have been called.', () => {
    const payload = 'test';
    const claimDetails = JwtClaimDetails.create(payload);
    spyOn(claimDetails, 'verify');
    expect(claimDetails.verify).not.toHaveBeenCalled();
  });

  it('should expect verify to have been called.', async done => {
    // set up: creates a signed JWS to verify.
    const jws = new JwsToken({test: 'test'}, new CryptoFactory([new RsaCryptoSuite()]));
    const rsaPrivateKey = await PrivateKeyRsa.generatePrivateKey('test');
    const rsaPublicKey = rsaPrivateKey.getPublicKey();
    const signedJws = await jws.sign(rsaPrivateKey);

    const claimDetails = JwtClaimDetails.create(signedJws);
    console.log(signedJws);
    spyOn(claimDetails, 'verify').and.callThrough();
    const verifiedData = await claimDetails.verify(rsaPublicKey);
    expect(claimDetails.verify).toHaveBeenCalled();
    expect(verifiedData).toBeDefined();
    console.log(verifiedData);
    done();
  });

});
