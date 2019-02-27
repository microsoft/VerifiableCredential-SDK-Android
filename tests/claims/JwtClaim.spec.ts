/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Claim from '../../src/claims/JwtClaim';

// describe('Claim', () => {

//   it('should construct new instance when provided an jwt string', async done => {
//     const claim = new Claim('jwtDummy');
//     expect(claim).toBeDefined();
//     expect(claim.jwt).toEqual('jwtDummy');
//     expect(claim.uiRef).toBeUndefined();
//     done();
//   });

//   it('should construct new instance when provided a claim object', async done => {
//     const uiRef = { dummyui: 'dummy' };
//     const claimObj = {jwt: 'jwtDummy', uiRef};
//     const claim = new Claim(claimObj);
//     expect(claim).toBeDefined();
//     expect(claim.jwt).toEqual('jwtDummy');
//     expect(claim.uiRef).toEqual(uiRef);
//     done();
//   });

//   it('should construct new instance when provided an jwt string', async done => {
//     const claim = Claim.create('jwtDummy', { dummyui: 'dummy' });
//     expect(claim).toBeDefined();
//     expect(claim.jwt).toEqual('jwtDummy');
//     expect(claim.uiRef).toEqual({ dummyui: 'dummy' });
//     done();
//   });

//   it('should get the UI object from the claim instance', async done => {
//     const claimObj = {jwt: 'jwtDummy', uiRef: { dummyui: 'dummy' }};
//     const claim = new Claim(claimObj);
//     expect(claim).toBeDefined();
//     expect(claim.getClaimUI()).toEqual({ dummyui: 'dummy' });
//     done();
//   });

//   it('should throw an error if there is no ui reference for a claim', async done => {
//     const claim = new Claim('jwtDummy');
//     expect(claim).toBeDefined();
//     expect(() => claim.getClaimUI()).toThrow(new Error('No UI Reference for Claim'));
//     done();
//   });


// });

