/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterfaces, { HubInterfacesOptions, HubInterface } from './HubInterfaces';

/**
* A Class that does CRUD operations for storing items as Permissions in the Hub
*/
export default class Permissions extends HubInterfaces {

  constructor (hubMethodsOptions: HubInterfacesOptions) {
    hubMethodsOptions.hubInterface = HubInterface.Permissions;
    super(hubMethodsOptions);
  }
}