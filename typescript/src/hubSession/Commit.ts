/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { HubInterfaceType, CommitStrategyType } from '..';
import { Operation } from '../hubInterfaces/HubInterface';
 
 /**
  * Fields that can be specified when creating a new commit.
  */
 export interface ICommitFields {
 
   /**
    * Gets or sets the time stamp of the commit
    */
    committed_at: string;
 
   /**
    * Gets or sets the issuer of the commit, the DID of the persona.
    */
    iss: string;
 
   /**
    * Gets or sets the identifier of the hub owner of the commit
    */
    sub: string;
 
   /**
    * Gets or sets the hub interface type for the commit
    */
    interface: HubInterfaceType;
 
   /**
    * Gets or sets the context for the commit
    */
    context: string;
 
   /**
    * Gets or sets the commit type
    */
    type: string;
 
   /**
    * Gets or sets the commit operation
    */
    operation: Operation;
     
   /** 
    * Gets or sets the commit strategy type
    */
    commit_strategy: CommitStrategyType;
 
   /** 
    * The application-specific commit payload. 
    */
   payload: object | string;
 
   /**
    * Identifier for the object
    */
   object_id: string | undefined;

  // Make the type indexable
  [key: string]: any;
 
 }
 
 /**
  * Represents a new (i.e pending, unsigned) commit which will create, update, or delete an object in
  * a user's Identity Hub.
  */
 export default class Commit {
 
   private fields: ICommitFields;
 
   constructor(fields: ICommitFields) {
     this.fields = fields;
   }
 
   /**
    * Verifies whether the currently set fields constitute a valid commit which can be
    * signed/encrypted and stored in an Identity Hub.
    *
    * Throws an error if the commit is not valid.
    *
    * need: Move validation logic to hub-common-js repository to be shared with hub-node-core.
    */
   public validate() {
     if (!this.fields.iss) {
       throw new Error("Commit must specify the iss field.");
     }
 
     const requiredStrings = ['interface', 'context', 'type', 'committed_at', 'commit_strategy', 'sub'];
     requiredStrings.forEach((field) => {
       if (!(<any>this.fields)[field] || typeof (<any>this.fields)[field] !== 'string' || (<any>this.fields)[field].length === 0) {
         throw new Error(`Commit '${field}' field must be a non-zero-length string.`);
       }
     });
 
     if (!this.fields.operation) {
       throw new Error("Commit 'operation' field must be specified.");
     }
 
     if (['create', 'update', 'delete'].indexOf(this.fields.operation) === -1) {
       throw new Error("Commit 'operation' field must be one of create, update, or delete.");
     }
 
     if (this.fields.operation === 'create') {
       if (this.fields.object_id !== undefined) {
         throw new Error("Commit 'object_id' field must not be specified when operation is 'create'.");
       }
     } else {
       if (!this.fields.object_id) {
         throw new Error(`Commit 'object_id' field must be specified when operation is '${this.fields.operation}'.`);
       }
     }
 
     if (!this.fields.payload) {
       throw new Error("Commit must specify the 'payload' field.");
     }
 
     if (['string', 'object'].indexOf(typeof this.fields.payload) === -1) {
       throw new Error(`Commit payload must be string or object, ${typeof this.fields.payload} given.`);
     }
   }
 
   /**
    * Returns true if the validate() method would pass without error.
    */
   public isValid() {
     try {
       this.validate();
       return true;
     } catch (err) {
       return false;
     }
   }
 
   /**
    * Returns the fields of the commit.
    */
   getCommitFields(): Partial<ICommitFields> {
     return this.fields;
   }
 
   /**
    * Returns the application-specific payload for this commit.
    */
   getPayload(): any {
     return this.fields.payload;
   }
 }
