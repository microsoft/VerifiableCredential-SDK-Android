/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import { alter, explain } from './TestUtils';
 import Commit, { ICommitFields } from '../../src/hubSession/Commit';
 import { HubInterfaceType, CommitStrategyType } from '../../src';
import { Operation } from '../../src/hubInterfaces/HubInterface';
 
 const commitFields: ICommitFields = {
   interface: HubInterfaceType.Collections,
   context: 'schema.org',
   type: 'MusicPlaylist',
   operation: Operation.Create,
   committed_at: '2019-01-01',
   commit_strategy: CommitStrategyType.Basic,
   iss: 'did:example:sub.id',
   sub: 'did:example:sub.id',
   payload: {
     title: 'My Playlist',
   },
   object_id: undefined
 };
 
 describe('Commit', () => {
 
   let commit: Commit;
 
   beforeAll(() => {
     commit = new Commit(commitFields);
   });
 
   describe('validate', () => {
 
     const invalidStrings = ['', null, undefined, true, false, 7, [], [''], {}];
 
     const invalidCases: {[field: string]: any[]} = {
       //'interface': invalidStrings,
       'context': invalidStrings,
       'type': invalidStrings,
       'committed_at': invalidStrings,
       //'commit_strategy': invalidStrings,
       'sub': invalidStrings,
       'operation': [...invalidStrings, 'other'],
       'payload': [true, false, null, undefined, 77],
     };
 
     Object.keys(invalidCases).forEach((field) => {
       const cases = invalidCases[field];
       cases.forEach(invalidValue => {
         it(`should reject '${field}' set to ${explain(invalidValue)}`, async () => {
           const alteredFields = alter(commitFields, { [field]: invalidValue });
           const commit = new Commit(alteredFields);
           const result = commit.isValid();
           expect(result).toBeFalsy();
         });
       });
     });
 
     it('should ensure object_id is not set for a create commit', async () => {
 
       const alteredFields = alter(commitFields, {
         'operation': 'create',
         'object_id': 'abc'
       });
 
       const commit = new Commit(alteredFields);
       expect(commit.isValid()).toBeFalsy();
 
     });
 
     it('should ensure object_id is set for an update/delete commit', async () => {
 
       ['update', 'delete'].forEach((operation) => {
         const alteredFields = alter(commitFields, {
           'operation': operation,
           'object_id': undefined
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
 
   describe('getPayload()', () => {
     it('should return the payload', async () => {
       expect(commit.getPayload()).toEqual(commitFields.payload);
     });
   });
 });
