/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterface, { HubInterfaceOptions, HubInterfaceType } from './HubInterface';
import { PERMISSION_GRANT_CONTEXT, PERMISSION_GRANT_TYPE } from '../hubSession/objects/IPermissionGrant';

/**
* A Class that does CRUD operations for storing items as Permissions in the Hub
*/
export default class Permissions extends HubInterface {

  constructor (hubInterfaceOptions: HubInterfaceOptions) {
    hubInterfaceOptions.hubInterface = HubInterfaceType.Permissions;
    hubInterfaceOptions.context = PERMISSION_GRANT_CONTEXT;
    hubInterfaceOptions.type = PERMISSION_GRANT_TYPE;
    super(hubInterfaceOptions);
  }
}
