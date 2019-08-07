/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { HubInterfaceOptions, HubInterfaceType } from "../../src/hubInterfaces/HubInterface";
import Identifier from "../../src/Identifier";
import Collections from "../../src/hubInterfaces/Collections";
import Permissions from "../../src/hubInterfaces/Permissions";
import Actions from "../../src/hubInterfaces/Actions";
import Profile from "../../src/hubInterfaces/Profile";
import UserAgentError from "../../src/UserAgentError";
import HubClientMock from "./HubClientMock";
import { UserAgentOptions } from "../../src";
import { PERMISSION_GRANT_TYPE, PERMISSION_GRANT_CONTEXT } from "../../src/hubSession/objects/IPermissionGrant";

describe('Hub Interface', () => {

  let options: HubInterfaceOptions

  const identifier = new Identifier('did:test:12345', new UserAgentOptions());
  (<UserAgentOptions>identifier.options).cryptoOptions.signingKeyReference = 'testKey';

  beforeEach(() => {

    options = new HubInterfaceOptions();
    options.context = 'https://schema.org/test';
    options.type = 'test';
    options.clientIdentifier = identifier;
    options.hubOwner = identifier;
  });

  describe('Collections', () => {
    it('should create a new Instance of Collections', () => {
      const collections = new Collections(options);
      expect(collections).toBeDefined();
      expect(collections.type).toBe('test');
      expect(collections.commitStrategy).toBe('basic');
      expect(collections.hubInterface).toBe(HubInterfaceType.Collections);
      expect(collections.context).toBe('https://schema.org/test');
    });

    it('should throw error if context is not defined', () => {
      try {
        delete options.context;
        const collections = new Collections(options);
        // should not get passed this point.
        fail();
        console.log(collections);
      } catch(error) {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Hub Interface Options missing context parameter');
      }
    });

    it('should throw error if type is not defined', () => {
      try {
        delete options.type;
        const collections = new Collections(options);
        // should not get passed this point.
        fail();
        console.log(collections);
      } catch(error) {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Hub Interface Options missing type parameter');
      }
    });

    it('should add object to a hub', async () => {
      const collections = new Collections(options);
      spyOn(collections.hubClient, 'commit').and.stub
      const commit = await collections.addObject('test object');
    });
  });

  describe('Actions', () => {
    it('should create a new Instance of Actions', () => {
      const collections = new Actions(options);
      expect(collections).toBeDefined();
      expect(collections.type).toBe('test');
      expect(collections.commitStrategy).toBe('basic');
      expect(collections.hubInterface).toBe(HubInterfaceType.Actions);
      expect(collections.context).toBe('https://schema.org/test');
    });
  });

  describe('Permissions', () => {
    it('should create a new Instance of Permissions', () => {
      const collections = new Permissions(options);
      expect(collections).toBeDefined();
      expect(collections.type).toBe(PERMISSION_GRANT_TYPE);
      expect(collections.commitStrategy).toBe('basic');
      expect(collections.hubInterface).toBe(HubInterfaceType.Permissions);
      expect(collections.context).toBe(PERMISSION_GRANT_CONTEXT);
    });
  });

  describe('Profile', () => {
    it('should create a new Instance of Profile', () => {
      const collections = new Profile(options);
      expect(collections).toBeDefined();
      expect(collections.type).toBe('test');
      expect(collections.commitStrategy).toBe('basic');
      expect(collections.hubInterface).toBe(HubInterfaceType.Profile);
      expect(collections.context).toBe('https://schema.org/test');
    });
  });
});
